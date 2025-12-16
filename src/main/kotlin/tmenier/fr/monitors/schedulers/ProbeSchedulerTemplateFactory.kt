package tmenier.fr.monitors.schedulers

import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.common.utils.logger
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors

@ApplicationScoped
class ProbeSchedulerTemplateFactory(
    private val probeSchedulerFactory: ProbeSchedulerFactory
) {
    val probeTaskContext = CoroutineScope(
        Executors.newScheduledThreadPool(4).asCoroutineDispatcher() + CoroutineName("ProbeTask") + SupervisorJob()
    )

    @Scheduled(every = "10s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun runScheduledProbes() {
        val probes = ProbesEntity.getActiveProbes()
        val now = LocalDateTime.now()

        probeTaskContext.launch {
            probes
                .filter { shouldRunProbe(it, now) }
                .forEach { probe ->
                    launch {
                        try {
                            val protocolHandler = probeSchedulerFactory.getProtocol(probe.protocol)
                            if (protocolHandler != null) {
                                val result = protocolHandler.execute(probe)
                                probe.handleProbeResult(result, now)
                            } else {
                                logger.warn("Unknown probe protocol: ${probe.protocol}")
                            }
                        } catch (e: Exception) {
                            logger.error("Error executing probe id=${probe.id}: ${e.message}", e)
                        }
                    }
                }
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