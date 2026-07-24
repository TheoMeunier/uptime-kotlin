package tmenier.fr

import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.dtos.StoreProbeCheckTaskDto
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

@ApplicationScoped
class ProbeSchedulerService(
    private val probeRepository: ProbeRepository,
    private val probeCheckTaskRepository: ProbeCheckTaskRepository
) {

    @ConfigProperty(name = "scheduler.strategy", defaultValue = "none")
    private lateinit var strategy: String

    @ConfigProperty(name = "scheduler.worker.name", defaultValue = "none")
    private lateinit var myRegion: String

    private val schedulerScope =
        CoroutineScope(
            Executors.newScheduledThreadPool(4).asCoroutineDispatcher() +
                CoroutineName("ProbeSchedulerService") +
                SupervisorJob(),
        )

    private val scheduledProbes = ConcurrentHashMap<UUID, Job>()


    @Scheduled(every = "5s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun syncProbeJobs() {
        if (strategy != "database") return

        val probes = probeRepository.getActiveProbes()
        val activeIds = probes.filter { it.enabled }.map { it.id }.toSet()

        scheduledProbes.keys.forEach { probeId ->
            if (probeId !in activeIds) {
                scheduledProbes.remove(probeId)?.cancel()
                logger.info { "Stopped scheduling job for probe $probeId (disabled or deleted)" }
            }
        }

        probes.filter { it.enabled }.forEach { probe ->
            if (!scheduledProbes.containsKey(probe.id)) {
                scheduleProbe(probe.id)
                logger.info { "Started scheduling job for probe ${probe.id}" }
            }
        }
    }

    private fun scheduleProbe(probeId: UUID) {
        val job =
            schedulerScope.launch {
                while (isActive) {
                    val probe = probeRepository.findById(probeId)?.let {
                        tmenier.fr.databases.mappers.ProbeMapper.toDto(it)
                    }

                    if (probe == null || !probe.enabled) {
                        logger.info { "Probe $probeId disabled or deleted, stopping scheduler job" }
                        break
                    }

                    val now = LocalDateTime.now()
                    val nextRun = calculateNextRun(probe, now)
                    val delayMs = Duration.between(now, nextRun).toMillis()

                    if (delayMs > 0) {
                        delay(delayMs.milliseconds)
                    }

                    try {
                        createCheckTask(probe)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to create check_task for probe $probeId" }
                    }
                }
            }

        scheduledProbes[probeId] = job
    }


    private fun createCheckTask(probe: ProbeDTO) {
        val firstRegion = probe.regionsOrder?.firstOrNull() ?: myRegion

        val task = StoreProbeCheckTaskDto(
            probeId = probe.id,
            region = firstRegion,
            attemptNumber = 1,
            scheduleAt = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime(),
        )
        probeCheckTaskRepository.store(task)
    }

    private fun calculateNextRun(probe: ProbeDTO, from: LocalDateTime): LocalDateTime {
        val lastRun = probe.lastRun ?: return from.plusSeconds(5)

        val intervalSeconds = probe.interval
        var nextRun = lastRun.plusSeconds(intervalSeconds.toLong())

        while (nextRun.isBefore(from)) {
            nextRun = nextRun.plusSeconds(intervalSeconds.toLong())
        }

        return nextRun
    }

    @PreDestroy
    fun cleanup() = schedulerScope.cancel()
}
