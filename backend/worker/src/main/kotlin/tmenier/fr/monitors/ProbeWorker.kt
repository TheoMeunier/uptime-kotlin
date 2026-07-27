package tmenier.fr.monitors

import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import java.net.InetAddress
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@ApplicationScoped
class ProbeWorker(
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
    private val probeWorkerService: ProbeWorkerService,
) {
    @ConfigProperty(name = "scheduler.strategy", defaultValue = "none")
    lateinit var strategy: String

    @ConfigProperty(name = "scheduler.worker.name", defaultValue = "default")
    lateinit var region: String

    @ConfigProperty(name = "scheduler.worker.concurrency", defaultValue = "1")
    var concurrency: Int = 1

    private val leaseDuration = Duration.ofMinutes(2)
    private val activeTasks = AtomicInteger()
    private val activeTaskIds = ConcurrentHashMap.newKeySet<java.util.UUID>()
    private val threadSequence = AtomicInteger()
    private lateinit var executor: ExecutorService
    private val workerId by lazy { "$region-${InetAddress.getLocalHost().hostName}" }

    @PostConstruct
    fun started() {
        require(concurrency > 0) { "scheduler.worker.concurrency must be greater than zero" }
        executor =
            Executors.newFixedThreadPool(concurrency) { runnable ->
                Thread(runnable, "probe-worker-${threadSequence.incrementAndGet()}")
            }
        logger.info {
            "Probe worker started: workerId=$workerId, region=$region, strategy=$strategy, " +
                "concurrency=$concurrency, leaseDuration=${leaseDuration.seconds}s"
        }
    }

    @Scheduled(every = "1s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun executeDueTasks() {
        if (strategy != "database") return

        val capacity = concurrency - activeTasks.get()
        if (capacity <= 0) return

        val tasks =
            try {
                probeCheckTaskRepository.claimPendingTasks(
                    region = region,
                    workerId = workerId,
                    limit = capacity,
                    leaseDuration = leaseDuration,
                )
            } catch (error: Exception) {
                logger.error(error) { "Failed to claim Probe Check tasks for region $region" }
                return
            }

        if (tasks.isNotEmpty()) {
            logger.info {
                "Claimed ${tasks.size} Probe Check task(s): " +
                    tasks.joinToString { task ->
                        "${task.id}(probe=${task.probeId},attempt=${task.attemptNumber})"
                    }
            }
        }

        tasks.forEach { task ->
            activeTasks.incrementAndGet()
            activeTaskIds.add(task.id)
            executor.submit {
                try {
                    logger.info {
                        "Starting Probe Check task ${task.id}: probe=${task.probeId}, " +
                            "attempt=${task.attemptNumber}, region=${task.region}"
                    }
                    probeWorkerService.execute(task, workerId)
                    logger.info { "Finished Probe Check task ${task.id}" }
                } catch (error: Exception) {
                    logger.error(error) { "Technical failure executing Probe Check task ${task.id}" }
                    probeCheckTaskRepository.markTechnicalFailure(
                        taskId = task.id,
                        workerId = workerId,
                        message = error.message ?: "Unknown Probe Check processing error",
                    )
                } finally {
                    activeTaskIds.remove(task.id)
                    activeTasks.decrementAndGet()
                }
            }
        }
    }

    @Scheduled(every = "30s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun maintainLeases() {
        if (strategy != "database") return
        probeCheckTaskRepository.renewLeases(workerId, leaseDuration, activeTaskIds)
        probeCheckTaskRepository.deadLetterExpiredLeases()
    }

    @PreDestroy
    fun close() {
        logger.info {
            "Stopping Probe worker $workerId with ${activeTasks.get()} active task(s)"
        }
        executor.shutdownNow()
    }
}
