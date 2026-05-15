package tmenier.fr.schedulers

import io.quarkus.arc.lookup.LookupIfProperty
import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.notifications.NotificationService
import tmenier.fr.schedulers.services.SaveProbeMonitor
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.milliseconds

@LookupIfProperty(name = "quarkus.scheduler.strategy", stringValue = "db-lock")
@ApplicationScoped
class ProbeSchedulerTemplateFactory(
    private val probeSchedulerFactory: ProbeSchedulerFactory,
    private val saveProbeMonitorLog: SaveProbeMonitor,
    private val notificationService: NotificationService,
    private val probeRepository: ProbeRepository,
) {
    private val probeScope =
        CoroutineScope(
            Executors.newScheduledThreadPool(4).asCoroutineDispatcher() +
                CoroutineName("ProbeTask") +
                SupervisorJob(),
        )

    private val scheduledProbes = ConcurrentHashMap<UUID, Job>()
    private val runningProbes = ConcurrentHashMap.newKeySet<UUID>()

    @ConfigProperty(name = "quarkus.scheduler.strategy", defaultValue = "none")
    private lateinit var strategy: String

    @Scheduled(every = "5s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun runScheduledProbes() {
        logger.debug { "Running scheduled probes with strategy: $strategy" }
        if (strategy != "db-lock") return

        val probes = probeRepository.getActiveProbes()
        val activeProbeIds = probes.filter { it.enabled }.map { it.id }.toSet()

        scheduledProbes.keys.forEach { probeId ->
            if (probeId !in activeProbeIds) {
                scheduledProbes.remove(probeId)?.cancel()
                logger.info { "Stopped probe $probeId (disabled or deleted)" }
            }
        }

        probes.filter { it.enabled }.forEach { probe ->
            if (!scheduledProbes.containsKey(probe.id)) {
                scheduleProbe(probe.id)
                logger.info { "Started probe ${probe.id}" }
            }
        }
    }

    private fun scheduleProbe(probeId: UUID) {
        val job =
            probeScope.launch {
                while (isActive) {
                    val probe =
                        loadProbe(probeId)
                            ?: break

                    if (!probe.enabled) {
                        logger.info { "Probe $probeId disabled, stopping job" }
                        break
                    }

                    val now = LocalDateTime.now()
                    val nextRun = calculateNextRun(probe, now)
                    val delayMs = Duration.between(now, nextRun).toMillis()

                    if (delayMs > 0) {
                        delay(delayMs.milliseconds)
                    }

                    if (!runningProbes.add(probeId)) {
                        continue
                    }

                    try {
                        executeWithRetry(probe)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.error(e) { "Unexpected error executing probe $probeId" }
                    } finally {
                        runningProbes.remove(probeId)
                    }
                }
            }

        scheduledProbes[probeId] = job
    }

    private suspend fun executeWithRetry(probe: ProbeDTO) {
        val handler =
            probeSchedulerFactory.getProtocol(probe.protocol)
                ?: run {
                    logger.warn { "Unknown protocol ${probe.protocol}" }
                    return
                }

        @Suppress("UNCHECKED_CAST")
        val typeHandler = handler as ProbeSchedulerInterfaceType<Any>

        val maxAttempts = probe.retry + 1

        repeat(maxAttempts) { attempt ->
            val now = LocalDateTime.now()
            val isLastAttempt = attempt == maxAttempts - 1

            val result = typeHandler.execute(probe, probe.content, isLastAttempt)

            when (result.status) {
                ProbeMonitorLogStatus.SUCCESS -> {
                    logger.info {
                        "Probe ${probe.id} succeeded after ${attempt + 1} attempt(s)"
                    }
                    saveAndNotify(probe.id, now, result, probe.status)
                    return
                }

                ProbeMonitorLogStatus.WARNING,
                ProbeMonitorLogStatus.FAILURE,
                    -> {
                    logger.warn {
                        "Probe ${probe.id} ${result.status} on attempt ${attempt + 1}/$maxAttempts"
                    }

                    if (isLastAttempt) {
                        saveAndNotify(
                            probe.id,
                            now,
                            result.copy(status = ProbeMonitorLogStatus.FAILURE),
                            probe.status,
                        )
                        return
                    }

                    saveProbeMonitorLog.saveProbeMonitorLog(probe.id, now, result)
                }

                else -> {}
            }

            delay((probe.interval * 1000L).milliseconds)
        }
    }

    private suspend fun saveAndNotify(
        probeId: UUID,
        now: LocalDateTime,
        result: ProbeResult,
        status: ProbeMonitorLogStatus,
    ) {
        withContext(Dispatchers.IO) {
            saveProbeMonitorLog.saveProbeMonitorLog(probeId, now, result)
        }

        notificationService.sendNotification(probeId, result, status)
    }

    private suspend fun loadProbe(probeId: UUID): ProbeDTO =
        withContext(Dispatchers.IO) {
            withRequestContext {
                ProbeMapper.toDto(probeRepository.findById(probeId))
            } as ProbeDTO
        }

    private fun calculateNextRun(
        probe: ProbeDTO,
        from: LocalDateTime,
    ): LocalDateTime {
        val lastRun = probe.lastRun

        if (lastRun === null) return from.plusSeconds(5)

        val intervalSeconds = probe.interval
        var nextRun = lastRun.plusSeconds(intervalSeconds.toLong())

        while (nextRun.isBefore(from)) {
            nextRun = nextRun.plusSeconds(intervalSeconds.toLong())
        }

        return nextRun
    }

    @ActivateRequestContext
    fun <T> withRequestContext(block: () -> T): ProbeDTO? = block() as ProbeDTO?

    @PreDestroy
    fun cleanup() = probeScope.cancel()
}
