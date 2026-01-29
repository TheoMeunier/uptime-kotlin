package tmenier.fr.monitors.schedulers

import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import kotlinx.coroutines.*
import tmenier.fr.common.utils.logger
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.entities.mapper.ProbeContentMapper
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.notifications.NotificationService
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import tmenier.fr.monitors.services.SaveProbeMonitor
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@ApplicationScoped
class ProbeSchedulerTemplateFactory(
    private val probeSchedulerFactory: ProbeSchedulerFactory,
    private val saveProbeMonitorLog: SaveProbeMonitor,
    private val notificationService: NotificationService,
) {
    private val probeScope =
        CoroutineScope(
            Executors.newScheduledThreadPool(4).asCoroutineDispatcher() +
                    CoroutineName("ProbeTask") +
                    SupervisorJob(),
        )

    private val scheduledProbes = ConcurrentHashMap<UUID, Job>()
    private val runningProbes = ConcurrentHashMap.newKeySet<UUID>()

    @Scheduled(every = "5s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun runScheduledProbes() {
        val probes = ProbesEntity.getActiveProbes()
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
                        delay(delayMs)
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

    private suspend fun executeWithRetry(probe: ProbesEntity) {
        val content = ProbeContentMapper.toDto(probe)
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

            val result = typeHandler.execute(probe, content, isLastAttempt)

            when (result.status) {
                ProbeMonitorLogStatus.SUCCESS -> {
                    logger.info {
                        "Probe ${probe.id} succeeded after ${attempt + 1} attempt(s)"
                    }
                    saveAndNotify(probe.id, now, result)
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
                        )
                        return
                    }

                    saveOnly(probe.id, now, result)
                }

                else -> {}
            }

            delay(probe.interval * 1000L)
        }
    }

    private suspend fun saveOnly(
        probeId: UUID,
        now: LocalDateTime,
        result: ProbeResult,
    ) = withTransaction {
        ProbesEntity.findById(probeId)?.let {
            saveProbeMonitorLog.saveProbeMonitorLog(it, now, result)
        }
    }

    private suspend fun saveAndNotify(
        probeId: UUID,
        now: LocalDateTime,
        result: ProbeResult,
    ) = withTransaction {
        ProbesEntity.findById(probeId)?.let {
            val previousStatus = it.status
            saveProbeMonitorLog.saveProbeMonitorLog(it, now, result)
            notificationService.sendNotification(it.id, result, previousStatus)
        }
    }

    private suspend fun loadProbe(probeId: UUID): ProbesEntity? =
        withContext(Dispatchers.IO) {
            withRequestContext {
                ProbesEntity.findById(probeId)
            }
        }

    suspend fun <T> withTransaction(block: suspend () -> T): T =
        withContext(Dispatchers.IO) {
            withRequestContext {
                QuarkusTransaction.requiringNew().call {
                    runBlocking { block() }
                }
            }
        } as T

    private fun calculateNextRun(
        probe: ProbesEntity,
        from: LocalDateTime,
    ): LocalDateTime {
        val lastRun = probe.lastRun

        if (lastRun == null) return from.plusSeconds(5)

        val intervalSeconds = probe.interval.toLong()
        val nextRun = lastRun.plusSeconds(intervalSeconds)

        if (nextRun.isAfter(from)) {
            return nextRun
        }

        val elapsed = Duration.between(lastRun, from).seconds
        val missedIntervals = (elapsed / intervalSeconds) + 1

        return lastRun.plusSeconds(missedIntervals * intervalSeconds)
    }

    @ActivateRequestContext
    fun <T> withRequestContext(block: () -> T): T = block()

    @PreDestroy
    fun cleanup() = probeScope.cancel()
}
