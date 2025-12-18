package tmenier.fr.monitors.schedulers

import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.common.utils.logger
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.services.SaveProbeMonitor
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

@ApplicationScoped
class ProbeSchedulerTemplateFactory(
    private val probeSchedulerFactory: ProbeSchedulerFactory,
    private val saveProbeMonitorLog: SaveProbeMonitor,
) {
    val probeTaskContext = CoroutineScope(
        Executors.newScheduledThreadPool(4).asCoroutineDispatcher() + CoroutineName("ProbeTask") + SupervisorJob()
    )

    private val runningProbes = ConcurrentHashMap.newKeySet<UUID>()

    @Scheduled(every = "2s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun runScheduledProbes() {
        val probes = ProbesEntity.getActiveProbes()
        val now = LocalDateTime.now()

        probeTaskContext.launch {
            probes
                .filter { shouldRunProbe(it, now) }
                .filter { it.enabled }
                .filter { runningProbes.add(it.id) }
                .forEach { probe ->
                    launch {
                        try {
                            executeWithRetry(probe, now)
                        } catch (e: Exception) {
                            logger.error("Error executing probe id=${probe.id}: ${e.message}", e)
                        } finally {
                            runningProbes.remove(probe.id)
                        }
                    }
                }
        }
    }

    private suspend fun executeWithRetry(probe: ProbesEntity, now: LocalDateTime) {
        val protocolHandler = probeSchedulerFactory.getProtocol(probe.protocol)

        if (protocolHandler == null) {
            logger.warn("Unknown probe protocol: ${probe.protocol}")
            return
        }

        repeat(probe.retry) { attempt ->
            val result = protocolHandler.execute(probe)

            if (result.status == ProbeMonitorLogStatus.SUCCESS) {
                logger.info("Probe ${probe.id} succeeded after ${attempt + 1} retries")
                return saveProbeMonitorLog.saveProbeMonitorLog(probe, now, result)
            }

            logger.warn("Probe ${probe.id} failed after ${attempt + 1} retries")

            if (attempt < probe.retry - 1) {
                delay(probe.intervalRetry * 1000L)
            }
        }

        val r = protocolHandler.execute(probe,  true)
        return withContext(Dispatchers.IO) {
            saveProbeMonitorLog.saveProbeMonitorLog(probe, now, r)
        }
    }

    private fun shouldRunProbe(probe: ProbesEntity, now: LocalDateTime): Boolean {
        if (probe.lastRun == null) return true

        val timeSinceLastRun = Duration.between(probe.lastRun, now)
        return timeSinceLastRun.seconds >= probe.interval
    }

    @PreDestroy
    fun cleanup() = probeTaskContext.cancel()
}