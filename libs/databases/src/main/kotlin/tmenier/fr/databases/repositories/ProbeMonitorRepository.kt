package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.StoreProbeMonitorLogDto
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.entities.ProbesMonitorsLogEntity
import tmenier.fr.databases.mappers.ProbeMapper
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class ProbeMonitorRepository {

    fun countByProbeAndPeriod(probeId: UUID, from: LocalDateTime, to: LocalDateTime): Long {
        return ProbesMonitorsLogEntity.countByProbeAndPeriod(probeId, from, to)
    }

    fun countSuccessByProbeAndPeriod(probeId: UUID, from: LocalDateTime, to: LocalDateTime): Long =
        ProbesMonitorsLogEntity.countSuccessByProbeAndPeriod(probeId, from, to)

    fun store(dto: StoreProbeMonitorLogDto) {
        val probe = ProbesEntity.findById(dto.probe.id) ?: throw NotFoundException("Probe ${dto.probe.id} not found")

        val entity = ProbesMonitorsLogEntity()
        entity.id = UUID.randomUUID()
        entity.runAt = dto.runAt
        entity.message = dto.message
        entity.status = dto.status
        entity.responseTime = dto.responseTime
        entity.probe = probe
        entity.persist()
    }
}
