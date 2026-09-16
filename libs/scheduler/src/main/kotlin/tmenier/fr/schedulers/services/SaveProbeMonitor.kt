package tmenier.fr.schedulers.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.utils.MonitoringClock
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.StoreProbeMonitorLogDto
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.MaintenanceOccurrenceRepository
import tmenier.fr.databases.repositories.ProbeMonitorRepository
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.notifications.services.NotificationService
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class SaveProbeMonitor(
    private val probeRepository: ProbeRepository,
    private val probeMonitorRepository: ProbeMonitorRepository,
    private val maintenanceOccurrenceRepository: MaintenanceOccurrenceRepository,
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

        val at = MonitoringClock.toInstant(runAt)
        val underMaintenance =
            maintenanceOccurrenceRepository.isProbeUnderMaintenance(
                probeId = probe.id,
                at = at,
            )

        val alertedStatus = probe.alertedStatus
        probe.status = result.status
        probe.lastRun = runAt

        result.tlsCheckedAt?.let { probe.tlsCheckedAt = it }
        result.tlsExpiresAt?.let { probe.tlsExpiresAt = it }

        probeMonitorRepository.store(
            StoreProbeMonitorLogDto(
                runAt = runAt,
                message = result.message,
                status = result.status,
                responseTime = result.responseTime,
                probe = ProbeMapper.toDto(probe),
                checkTaskId = checkTaskId,
                underMaintenance = underMaintenance,
            ),
        )

        if (underMaintenance) {
            logger.info {
                "Suppressed notifications for Probe Check $checkTaskId: probe=${probe.id} is under maintenance at $runAt " +
                    "(status=${result.status}, last announced=$alertedStatus)"
            }
            return true
        }

        notificationService.announce(
            probe = probe,
            checkTaskId = checkTaskId,
            result = result,
            at = at,
        )

        return true
    }
}
