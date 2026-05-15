package tmenier.fr

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Incoming
import tmenier.fr.common.dtos.NotificationJob
import tmenier.fr.common.dtos.ProbeJob
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.schedulers.ProbeSchedulerFactory
import tmenier.fr.schedulers.ProbeSchedulerInterfaceType
import tmenier.fr.schedulers.services.SaveProbeMonitor
import java.util.concurrent.Executors
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

@ApplicationScoped
class ProbeWorker(
    private val probeSchedulerFactory: ProbeSchedulerFactory,
    private val saveProbeMonitorLog: SaveProbeMonitor,
    private val probeRepository: ProbeRepository,
) {
    @ConfigProperty(name = "probe.scheduler.strategy", defaultValue = "none")
    private lateinit var strategy: String

    @Inject
    @Channel("notification-jobs-out")
    private lateinit var notificationEmitter: Emitter<NotificationJob>

    private val workerScope = CoroutineScope(
        Executors.newScheduledThreadPool(4).asCoroutineDispatcher() +
            CoroutineName("ProbeWorker") +
            SupervisorJob()
    )

    @Incoming("probe-jobs-in")
    @ActivateRequestContext
    fun handleProbeJob(job: ProbeJob) {
        if (strategy != "rabbitmq") return

        workerScope.launch {
            try {
                executeWithRetry(job)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error(e) { "Unexpected error executing probe ${job.probeId}" }
            }
        }
    }

    private suspend fun executeWithRetry(job: ProbeJob) {
        val probe = withContext(Dispatchers.IO) {
            ProbeMapper.toDto(probeRepository.findById(job.probeId))
        }

        val handler = probeSchedulerFactory.getProtocol(job.protocol)
            ?: run {
                logger.warn { "Unknown protocol ${job.protocol}" }
                return
            }

        @Suppress("UNCHECKED_CAST")
        val typeHandler = handler as ProbeSchedulerInterfaceType<Any>

        val maxAttempts = job.retry + 1

        repeat(maxAttempts) { attempt ->
            val isLastAttempt = attempt == maxAttempts - 1
            val result = typeHandler.execute(probe, job.content, isLastAttempt)

            when (result.status) {
                ProbeMonitorLogStatus.SUCCESS -> {
                    logger.info { "Probe ${job.probeId} succeeded after ${attempt + 1} attempt(s)" }
                    saveAndPublishNotification(job, result, probe.status)
                    return
                }

                ProbeMonitorLogStatus.WARNING,
                ProbeMonitorLogStatus.FAILURE -> {
                    logger.warn { "Probe ${job.probeId} ${result.status} on attempt ${attempt + 1}/$maxAttempts" }

                    if (isLastAttempt) {
                        saveAndPublishNotification(
                            job,
                            result.copy(status = ProbeMonitorLogStatus.FAILURE),
                            probe.status,
                        )
                        return
                    }

                    withContext(Dispatchers.IO) {
                        saveProbeMonitorLog.saveProbeMonitorLog(
                            job.probeId,
                            job.scheduledAt,
                            result,
                        )
                    }
                }

                else -> {}
            }

            delay((probe.retry * 1000L).milliseconds)
        }
    }

    private suspend fun saveAndPublishNotification(
        job: ProbeJob,
        result: ProbeResult,
        previousStatus: ProbeMonitorLogStatus,
    ) {
        withContext(Dispatchers.IO) {
            saveProbeMonitorLog.saveProbeMonitorLog(
                job.probeId,
                job.scheduledAt,
                result,
            )
        }

        notificationEmitter.send(
            NotificationJob(
                probeId = job.probeId,
                result = result,
                previousStatus = previousStatus,
            )
        )
    }
}
