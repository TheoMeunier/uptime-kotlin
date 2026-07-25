package tmenier.fr.schedulers

import io.quarkus.arc.lookup.LookupIfProperty
import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.schedulers.services.ProbeAttemptPolicy
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
    private val saveProbeMonitor: SaveProbeMonitor,
    private val probeRepository: ProbeRepository,
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
) {
    @ConfigProperty(name = "quarkus.scheduler.strategy", defaultValue = "none")
    lateinit var strategy: String

    private val scope =
        CoroutineScope(
            Executors.newScheduledThreadPool(4).asCoroutineDispatcher() +
                CoroutineName("StandaloneProbeChecks") +
                SupervisorJob(),
        )
    private val runningProbes = ConcurrentHashMap.newKeySet<UUID>()

    @Scheduled(every = "1s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun runDueProbes() {
        if (strategy != "db-lock") return

        val dueProbeIds =
            try {
                probeCheckTaskRepository.claimDueStandaloneProbes()
            } catch (error: Exception) {
                logger.error(error) { "Failed to claim standalone Probe Checks" }
                return
            }

        dueProbeIds.forEach { probeId ->
            if (!runningProbes.add(probeId)) return@forEach
            scope.launch {
                try {
                    executeChain(probeId)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    logger.error(error) { "Technical failure executing standalone Probe Check for $probeId" }
                } finally {
                    runningProbes.remove(probeId)
                }
            }
        }
    }

    private suspend fun executeChain(probeId: UUID) {
        var attemptNumber = 1

        while (true) {
            val probe = loadProbe(probeId)
            if (!probe.enabled) return

            val handler =
                probeSchedulerFactory.getProtocol(probe.protocol)
                    ?: throw IllegalArgumentException("Unknown Probe protocol ${probe.protocol}")

            @Suppress("UNCHECKED_CAST")
            val typedHandler = handler as ProbeSchedulerInterfaceType<Any>
            val isLastAttempt =
                probe.status == ProbeMonitorLogStatus.FAILURE ||
                    attemptNumber >= probe.retry + 1

            val result = typedHandler.execute(probe, probe.content, isLastAttempt)
            val completedAt = LocalDateTime.now()
            val durableResult =
                result.copy(
                    status =
                        ProbeAttemptPolicy.durableStatus(
                            resultStatus = result.status,
                            currentStatus = probe.status,
                            attemptNumber = attemptNumber,
                            retryCount = probe.retry,
                        ),
                    runAt = completedAt,
                )
            val saved =
                saveProbeMonitor.saveProbeMonitorLog(
                    probeId = probe.id,
                    runAt = completedAt,
                    result = durableResult,
                )

            if (!saved || durableResult.status != ProbeMonitorLogStatus.WARNING) return

            attemptNumber++
            awaitRetry(probeId, completedAt)
        }
    }

    private suspend fun awaitRetry(
        probeId: UUID,
        failedAt: LocalDateTime,
    ) {
        while (true) {
            val probe = loadProbe(probeId)
            if (!probe.enabled) return

            val retryAt = failedAt.plusSeconds(probe.intervalRetry.toLong())
            val remaining = Duration.between(LocalDateTime.now(), retryAt).toMillis()
            if (remaining <= 0) return
            delay(minOf(remaining, 1_000).milliseconds)
        }
    }

    private fun loadProbe(probeId: UUID): ProbeDTO =
        withRequestContext {
            ProbeMapper.toDto(probeRepository.findById(probeId))
        }

    @ActivateRequestContext
    fun <T> withRequestContext(block: () -> T): T = block()

    @PreDestroy
    fun cleanup() {
        scope.cancel()
    }
}
