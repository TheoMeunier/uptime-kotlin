package tmenier.fr.schedulers.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.StoreProbeMonitorLogDto
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeMonitorRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class SaveProbeMonitor(
    private val probeRepository: ProbeRepository,
    private val probeMonitorRepository: ProbeMonitorRepository,
) {
    @Transactional
    fun saveProbeMonitorLog(
        probeId: UUID,
        runAt: LocalDateTime,
        result: ProbeResult,
    ) {
        val probe = probeRepository.findById(probeId)

        setLastRun(probe, runAt, result.status)

        val dto =
            StoreProbeMonitorLogDto(
                runAt = runAt,
                message = result.message,
                status = result.status,
                responseTime = result.responseTime,
                probe = ProbeMapper.toDto(probe),
            )

        probeMonitorRepository.store(dto)
    }

    private fun setLastRun(
        probe: ProbesEntity,
        runAt: LocalDateTime,
        status: ProbeMonitorLogStatus,
    ) {
        if (status in updateStatus) {
            probe.status = status
        }

        probe.lastRun = runAt
        probe.persist()
    }

    private val updateStatus =
        setOf(
            ProbeMonitorLogStatus.SUCCESS,
            ProbeMonitorLogStatus.FAILURE,
        )
}
