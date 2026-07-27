package tmenier.fr.monitors

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.probes.QueueJobStatus
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.StoreProbeCheckTaskDto
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.databases.repositories.WorkerHeartbeatRepository
import tmenier.fr.schedulers.services.ProbeAttemptPolicy
import tmenier.fr.schedulers.services.SaveProbeMonitor
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@ApplicationScoped
class ProbeAttemptRecorder(
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
    private val probeRepository: ProbeRepository,
    private val workerHeartbeatRepository: WorkerHeartbeatRepository,
    private val saveProbeMonitor: SaveProbeMonitor,
) {
    @Transactional
    fun completeAttempt(
        taskId: UUID,
        workerId: String,
        result: ProbeResult,
        completedAt: LocalDateTime,
    ) {
        val task =
            probeCheckTaskRepository.findByIdForUpdate(taskId)
                ?: run {
                    logger.warn { "Cannot record Probe Check task $taskId: queue row no longer exists" }
                    return
                }
        if (task.status != QueueJobStatus.LEASED || task.claimedBy != workerId) {
            logger.warn {
                "Cannot record Probe Check task $taskId: status=${task.status}, " +
                    "leaseOwner=${task.claimedBy}, expectedOwner=$workerId"
            }
            return
        }

        val probe = probeRepository.findByIdForUpdate(task.probeId)
        if (!probe.enabled) {
            logger.info { "Deleting Probe Check task $taskId without result: probe ${probe.id} is disabled" }
            probeCheckTaskRepository.deleteTask(task)
            return
        }

        val durableStatus =
            ProbeAttemptPolicy.durableStatus(
                resultStatus = result.status,
                currentStatus = probe.status,
                attemptNumber = task.attemptNumber,
                retryCount = probe.retry,
            )
        val durableResult =
            result.copy(
                status = durableStatus,
                runAt = completedAt,
            )

        val stored =
            saveProbeMonitor.saveProbeMonitorLog(
                probeId = probe.id,
                runAt = completedAt,
                result = durableResult,
                checkTaskId = task.id,
            )

        probeCheckTaskRepository.deleteTask(task)

        if (!stored) {
            logger.warn {
                "Probe Check task $taskId was already persisted; deleted duplicate queue row"
            }
            return
        }

        logger.info {
            "Persisted Probe Check task $taskId: probe=${probe.id}, attempt=${task.attemptNumber}, " +
                "status=$durableStatus; deleted completed queue row"
        }

        if (durableStatus == ProbeMonitorLogStatus.WARNING) {
            val nextRegion = selectNextRegion(task.region, probe.regionsOrder)
            val retry =
                probeCheckTaskRepository.store(
                    StoreProbeCheckTaskDto(
                        probeId = probe.id,
                        region = nextRegion,
                        attemptNumber = task.attemptNumber + 1,
                        scheduleAt = completedAt.plusSeconds(probe.intervalRetry.toLong()),
                        availableAt =
                            completedAt
                                .plusSeconds(probe.intervalRetry.toLong())
                                .atZone(ZoneId.systemDefault())
                                .toInstant(),
                        previousFailedAt = completedAt.atZone(ZoneId.systemDefault()).toInstant(),
                    ),
                )
            logger.info {
                "Scheduled retry task ${retry.id}: probe=${probe.id}, " +
                    "attempt=${retry.attemptNumber}, region=$nextRegion, availableAt=${retry.availableAt}"
            }
        }
    }

    @Transactional
    fun discardDisabledTask(
        taskId: UUID,
        workerId: String,
    ) {
        val task = probeCheckTaskRepository.findByIdForUpdate(taskId) ?: return
        if (task.status == QueueJobStatus.LEASED && task.claimedBy == workerId) {
            probeCheckTaskRepository.deleteTask(task)
            logger.info { "Deleted Probe Check task $taskId for disabled probe ${task.probeId}" }
        }
    }

    private fun selectNextRegion(
        currentRegion: String,
        configuredRegions: com.fasterxml.jackson.databind.JsonNode?,
    ): String {
        val activeRegions = workerHeartbeatRepository.activeRegions()
        val configured =
            if (configuredRegions?.isArray == true) {
                configuredRegions.map { it.asText() }
            } else {
                emptyList()
            }
        val candidates =
            configured
                .filter { it in activeRegions }
                .ifEmpty { activeRegions }
                .ifEmpty { listOf(currentRegion) }

        if (candidates.size == 1) return candidates.first()
        val currentIndex = candidates.indexOf(currentRegion)
        return if (currentIndex >= 0) {
            candidates[(currentIndex + 1) % candidates.size]
        } else {
            candidates.first { it != currentRegion }
        }
    }
}
