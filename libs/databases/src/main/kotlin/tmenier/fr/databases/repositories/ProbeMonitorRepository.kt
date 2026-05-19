package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.StoreProbeMonitorLogDto
import tmenier.fr.databases.entities.ProbesMonitorsLogEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class ProbeMonitorRepository(
    private val probeRepository: ProbeRepository,
) : PanacheRepositoryBase<ProbesMonitorsLogEntity, UUID> {
    fun countByProbeAndPeriod(
        probeId: UUID,
        from: LocalDateTime,
        to: LocalDateTime,
    ): Long = count("probe.id = ?1 AND runAt >= ?2 AND runAt <= ?3", probeId, from, to)

    fun countSuccessByProbeAndPeriod(
        probeId: UUID,
        from: LocalDateTime,
        to: LocalDateTime,
    ): Long = count("probe.id = ?1 AND status = ?2 AND runAt >= ?3 AND runAt <= ?4", probeId, ProbeMonitorLogStatus.SUCCESS, from, to)

    fun store(dto: StoreProbeMonitorLogDto) {
        val probe = probeRepository.findById(dto.probe.id)

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
