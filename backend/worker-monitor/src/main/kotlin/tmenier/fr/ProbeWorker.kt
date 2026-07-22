package tmenier.fr

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.schedulers.ProbeSchedulerFactory
import tmenier.fr.schedulers.ProbeSchedulerInterfaceType
import java.net.InetAddress
import java.util.concurrent.Executors
import kotlin.coroutines.cancellation.CancellationException

@ApplicationScoped
class ProbeWorker(
    private val probeSchedulerFactory: ProbeSchedulerFactory,
    private val probeRepository: ProbeRepository,
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
    private val probeWorkerService: ProbeWorkerService,
    private val workerRegistry: WorkerRegistry
) {
    @ConfigProperty(name = "scheduler.strategy", defaultValue = "none")
    private lateinit var strategy: String

    @ConfigProperty(name = "scheduler.worker.name", defaultValue = "true")
    private lateinit var myRegion: String

    private val workerScope =
        CoroutineScope(
            Executors.newScheduledThreadPool(4).asCoroutineDispatcher() +
                CoroutineName("ProbeWorker") +
                SupervisorJob(),
        )

    @Scheduled(every = "2s")
    fun execute() {
        if (strategy != "database") {
            logger.warn { "Ignoring probe job because strategy is not database" }
            return
        }

        val tasks = try {
            val workerId = "${myRegion}-${InetAddress.getLocalHost().hostName}"
            probeCheckTaskRepository.claimPendingTasks(myRegion, workerId)
        } catch (e: Exception) {
            logger.error(e) { "Failed to claim pending probe tasks" }
            return
        }

        logger.info { "Claimed ${tasks?.size ?: 0} pending probe tasks for region $myRegion" }

        tasks?.forEach { task ->
            workerScope.launch {
                try {
                    val probe =
                        withContext(Dispatchers.IO) {
                            ProbeMapper.toDto(probeRepository.findById(task.probeId))
                        }

                    try {
                        val handler =
                            probeSchedulerFactory.getProtocol(probe.protocol)
                                ?: run {
                                    logger.warn { "Unknown protocol ${probe.protocol}" }
                                    return@launch
                                }

                        @Suppress("UNCHECKED_CAST")
                        val typeHandler = handler as ProbeSchedulerInterfaceType<Any>

                        if (workerRegistry.activeWorkerCount() > 1) {
                            probeWorkerService.executeSingleAttemptWithCascade(task, probe, typeHandler, myRegion)
                        } else {
                            probeWorkerService.executeLocalRetryLoop(task, probe, typeHandler)
                        }
                    } finally {
                        logger.debug { "Released lock for probe ${task.probeId}" }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.error(e) { "Unexpected error executing probe ${task.id}" }
                }
            }
        }
    }
}
