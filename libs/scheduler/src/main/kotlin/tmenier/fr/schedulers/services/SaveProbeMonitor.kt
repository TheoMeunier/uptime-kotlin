package tmenier.fr.schedulers.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.databases.dtos.StoreProbeMonitorLogDto
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeMonitorRepository
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.notifications.services.NotificationService
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class SaveProbeMonitor(
    private val probeRepository: ProbeRepository,
    private val probeMonitorRepository: ProbeMonitorRepository,
    private val notificationService: NotificationService,
) {
    @Transactional
    fun saveProbeMonitorLog(
        probeId: UUID,
        runAt: LocalDateTime,
        result: ProbeResult,
        checkTaskId: UUID = UUID.randomUUID(),
    ): Boolean {
        if (probeMonitorRepository.existsByCheckTaskId(checkTaskId)) return false

        val probe = probeRepository.findByIdForUpdate(probeId)
        if (!probe.enabled) return false

        val previousStatus = probe.status
        probe.status = result.status
        probe.lastRun = runAt

        probeMonitorRepository.store(
            StoreProbeMonitorLogDto(
                runAt = runAt,
                message = result.message,
                status = result.status,
                responseTime = result.responseTime,
                probe = ProbeMapper.toDto(probe),
                checkTaskId = checkTaskId,
            ),
        )

        notificationService.enqueueForTransition(
            probe = probe,
            checkTaskId = checkTaskId,
            result = result,
            previousStatus = previousStatus,
        )

        return true
    }
}
