package tmenier.fr.monitors.schedulers

import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.*
import tmenier.fr.common.utils.logger
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.notifications.NotificationService
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
    private val notificationService: NotificationService
) {
    val probeTaskContext = CoroutineScope(
        Executors.newScheduledThreadPool(4).asCoroutineDispatcher() + CoroutineName("ProbeTask") + SupervisorJob()
    )

    private val runningProbes = ConcurrentHashMap.newKeySet<UUID>()
    private val scheduledProbes = ConcurrentHashMap<UUID, Job>()

    @Scheduled(every = "5s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun runScheduledProbes() {
        val probes = ProbesEntity.getActiveProbes()
        val activeProbeIds = probes.filter { it.enabled }.map { it.id }.toSet()

        // Scheduler canceled probes
        scheduledProbes.keys.forEach { probeId ->
            if (probeId !in activeProbeIds) {
                scheduledProbes.remove(probeId)?.cancel()
                logger.info { "Cancelled probe $probeId (disabled or deleted)" }
            }
        }

        // Scheduler news probes
        probes.filter { it.enabled }.forEach { probe ->
            if (!scheduledProbes.containsKey(probe.id)) {
                scheduleProbe(probe)
            }
        }
    }

    private fun scheduleProbe(probe: ProbesEntity) {
        val job = probeTaskContext.launch {
            while (isActive) {
                val now = LocalDateTime.now()
                val nextRun = calculateNextRun(probe, now)
                val delayMs = Duration.between(now, nextRun).toMillis()

                if (delayMs > 0) {
                    delay(delayMs)
                }

                if (runningProbes.add(probe.id)) {
                    try {
                        executeWithRetry(probe, LocalDateTime.now())
                    } catch (e: Exception) {
                        logger.error { "Error executing probe id=${probe.id}: ${e.stackTraceToString()}" }
                    } finally {
                        runningProbes.remove(probe.id)
                    }
                }
            }
        }

        scheduledProbes[probe.id] = job
    }

    private suspend fun executeWithRetry(probe: ProbesEntity, now: LocalDateTime) {
        val protocolHandler = probeSchedulerFactory.getProtocol(probe.protocol)
        val maxAttempts = probe.retry + 1

        if (protocolHandler == null) {
            logger.warn { "Unknown probe protocol: ${probe.protocol}" }
            return
        }

        repeat(maxAttempts) { attempt ->
            val isLastAttempt = attempt == maxAttempts - 1
            val result = protocolHandler.execute(probe, isLastAttempt)

            when (result.status) {
                ProbeMonitorLogStatus.SUCCESS -> {
                    logger.info { "Probe ${probe.id} succeeded after ${attempt + 1} retries" }

                    notificationService.sendNotification(probe, result)
                    saveProbeMonitorLog.saveProbeMonitorLog(probe, now, result)
                    return
                }

                ProbeMonitorLogStatus.WARNING -> {
                    if (isLastAttempt) {
                        logger.error { "Probe ${probe.id} failed after $maxAttempts attempts" }

                        val failedResult = result.copy(
                            status = ProbeMonitorLogStatus.FAILURE
                        )

                        notificationService.sendNotification(probe, failedResult)
                        saveProbeMonitorLog.saveProbeMonitorLog(probe, now, failedResult)
                        return
                    }

                    logger.warn { "Probe ${probe.id} warning after ${attempt + 1}/$maxAttempts attempts" }
                    saveProbeMonitorLog.saveProbeMonitorLog(probe, now, result)
                }

                ProbeMonitorLogStatus.FAILURE -> {
                    logger.error { "Probe ${probe.id} failed on attempt ${attempt + 1}/$maxAttempts" }

                    if (isLastAttempt) {
                        notificationService.sendNotification(probe, result)
                        saveProbeMonitorLog.saveProbeMonitorLog(probe, now, result)
                        return
                    }
                }

                else -> {
                    if (isLastAttempt) {
                        notificationService.sendNotification(probe, result)
                        saveProbeMonitorLog.saveProbeMonitorLog(probe, now, result)
                        return
                    }
                }
            }

            delay(probe.interval * 1000L)
        }
    }

    private fun calculateNextRun(probe: ProbesEntity, from: LocalDateTime): LocalDateTime {
        val lastRun = probe.lastRun ?: return from
        val intervalSeconds = probe.interval
        var nextRun = lastRun.plusSeconds(intervalSeconds.toLong())

        while (nextRun.isBefore(from)) {
            nextRun = nextRun.plusSeconds(intervalSeconds.toLong())
        }

        return nextRun
    }

    @PreDestroy
    fun cleanup() = probeTaskContext.cancel()
}