package tmenier.fr.schedulers.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.dtos.StoreProbeMonitorLogDto
import tmenier.fr.databases.dtos.UpdateLastRunDto
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.entities.ProbesMonitorsLogEntity
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeMonitorRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class SaveProbeMonitor(
    private val probeRepository: ProbeRepository,
    private val probeMonitorRepository: ProbeMonitorRepository
) {
    @Transactional
    fun saveProbeMonitorLog(
        probeId: UUID,
        runAt: LocalDateTime,
        result: ProbeResult,
    ) {
        val manageProbe = ProbeMapper.toDto(probeRepository.findById(probeId))

        setLastRun(manageProbe, runAt, result.status)

        val dto = StoreProbeMonitorLogDto(
            runAt = runAt,
            message = result.message,
            status = result.status,
            responseTime = result.responseTime,
            probe = manageProbe,
        )

        probeMonitorRepository.store(dto)
    }

    private fun setLastRun(
        probe: ProbeDTO,
        runAt: LocalDateTime,
        status: ProbeMonitorLogStatus,
    ) {
        val updateStatus = setOf(ProbeMonitorLogStatus.SUCCESS, ProbeMonitorLogStatus.FAILURE)

        val dto = UpdateLastRunDto(
            id = probe.id,
            status = if (probe.status in updateStatus) status else null,
            lastRun = runAt,
        )

        probeRepository.updateLastRun(dto)
    }
}
