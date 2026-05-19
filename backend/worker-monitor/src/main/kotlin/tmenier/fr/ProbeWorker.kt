package tmenier.fr

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.redis.datasource.ReactiveRedisDataSource
import io.quarkus.redis.datasource.value.SetArgs
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Message
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
import java.time.Duration
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executors
import kotlin.coroutines.cancellation.CancellationException

@ApplicationScoped
class ProbeWorker(
    private val probeSchedulerFactory: ProbeSchedulerFactory,
    private val saveProbeMonitorLog: SaveProbeMonitor,
    private val probeRepository: ProbeRepository,
    private val objectMapper: ObjectMapper,
    private val redis: ReactiveRedisDataSource,
) {
    @ConfigProperty(name = "scheduler.strategy", defaultValue = "none")
    private lateinit var strategy: String

    @Inject
    @Channel("notification-jobs-out")
    private lateinit var notificationEmitter: Emitter<NotificationJob>

    private val workerScope =
        CoroutineScope(
            Executors.newScheduledThreadPool(4).asCoroutineDispatcher() +
                CoroutineName("ProbeWorker") +
                SupervisorJob(),
        )

    @Incoming("probe-jobs-in")
    @ActivateRequestContext
    fun handleProbeJob(message: Message<JsonObject>): CompletionStage<Void> {
        if (strategy != "rabbitmq") {
            return message.ack()
        }

        return try {
            val rawJson = message.payload.encode()
            val job = objectMapper.readValue(rawJson, ProbeJob::class.java)

            workerScope.launch {
                try {
                    executeWithRetry(job)
                    message.ack()
                } catch (e: CancellationException) {
                    message.ack()
                    throw e
                } catch (e: Exception) {
                    logger.error(e) { "Unexpected error executing probe ${job.probeId}" }
                    message.nack(e)
                }
            }

            message.ack()
        } catch (e: Exception) {
            logger.error(e) { "Failed to deserialize probe job" }
            message.ack()
        }
    }

    private suspend fun executeWithRetry(job: ProbeJob) {
        val probe =
            withContext(Dispatchers.IO) {
                ProbeMapper.toDto(probeRepository.findById(job.probeId))
            }

        val lockKey = "probe:running:${job.probeId}"
        val lockTtl = Duration.ofSeconds((probe.interval + 60).toLong())

        withContext(Dispatchers.IO) {
            redis
                .value(String::class.java)
                .set(lockKey, "1", SetArgs().ex(lockTtl))
                .await()
                .indefinitely()
        }

        try {
            val handler =
                probeSchedulerFactory.getProtocol(probe.protocol)
                    ?: run {
                        logger.warn { "Unknown protocol ${probe.protocol}" }
                        return
                    }

            @Suppress("UNCHECKED_CAST")
            val typeHandler = handler as ProbeSchedulerInterfaceType<Any>

            val maxAttempts = if (probe.status == ProbeMonitorLogStatus.FAILURE) 1 else probe.retry + 1

            repeat(maxAttempts) { attempt ->
                val isLastAttempt = attempt == maxAttempts - 1
                val result = typeHandler.execute(probe, probe.content, isLastAttempt)

                when (result.status) {
                    ProbeMonitorLogStatus.SUCCESS -> {
                        logger.info { "Probe ${job.probeId} ${result.status} after ${attempt + 1} attempt(s)" }
                        saveAndPublishNotification(job, result, probe.status)
                        return
                    }

                    ProbeMonitorLogStatus.WARNING,
                    ProbeMonitorLogStatus.FAILURE,
                    -> {
                        logger.warn {
                            "Probe ${probe.id} ${result.status} on attempt ${attempt + 1}/$maxAttempts"
                        }

                        if (isLastAttempt) {
                            saveAndPublishNotification(
                                job,
                                result.copy(status = ProbeMonitorLogStatus.FAILURE),
                                probe.status,
                            )
                            return
                        }

                        saveProbeMonitorLog.saveProbeMonitorLog(probe.id, job.scheduledAt, result)
                    }

                    else -> {}
                }
            }
        } finally {
            withContext(Dispatchers.IO) {
                redis
                    .value(String::class.java)
                    .getdel(lockKey)
                    .await()
                    .indefinitely()
            }

            logger.debug { "Released lock for probe ${job.probeId}" }
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

        try {
            notificationEmitter.send(
                NotificationJob(
                    probeId = job.probeId,
                    result = result,
                    previousStatus = previousStatus,
                ),
            )

            logger.info { "Probe ${job.probeId} notification sent" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to save probe monitor log" }
            throw e
        }
    }
}
