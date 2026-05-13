package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.dtos.ProbeOnOffDto
import tmenier.fr.databases.dtos.ProbeStatusDTO
import tmenier.fr.databases.dtos.StoreProbeDto
import tmenier.fr.databases.entities.NotificationsChannelEntity
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.ProbeContentMapper
import tmenier.fr.databases.mappers.ProbeMapper
import java.util.UUID

@ApplicationScoped
class ProbeRepository {

    fun findById(probeId: UUID): ProbesEntity = ProbesEntity.findById(probeId) ?: throw IllegalArgumentException("Probe not found")

    fun findByIdWithLogs(probeId: UUID, hour: Long = 1) = ProbesEntity.findByIdWithLogs(probeId, hour) ?: throw IllegalArgumentException("Probe not found")

    fun getProbesLastHour(): List<ProbeStatusDTO> = ProbesEntity.getProbesLastHour().map { ProbeMapper.toStatusDto(it) }

    fun getActiveProbes(): List<ProbeDTO> = ProbesEntity.getActiveProbes().map { ProbeMapper.toDto(it) }

    fun getAll(): List<ProbesEntity> = ProbesEntity.getAllProbes()

    fun attach(notifications: List<UUID>, probe: ProbesEntity) {
        val notificationsEntities = NotificationsChannelEntity.findByIds(notifications)
        probe.notifications.addAll(notificationsEntities)
    }

    fun delete(probeId: UUID) = ProbesEntity.delete(probeId)

    fun save(dto: StoreProbeDto, notifications: List<UUID>) {
        val entity = ProbeMapper.toEntity(dto)
        attach(notifications, entity)
        entity.persist()
    }

    fun update(dto: StoreProbeDto, notifications: List<UUID>) {
        val entity = findById(dto.id)
        entity.name = dto.name
        entity.interval = dto.interval
        entity.intervalRetry = dto.intervalRetry
        entity.retry = dto.retry
        entity.protocol = dto.protocol
        entity.enabled = dto.enabled
        entity.description = dto.description
        entity.content = ProbeContentMapper.toEntity(dto.content).first

        entity.notifications.clear()
        attach(notifications, entity)

        entity.persist()
    }

    fun onOff(dto: ProbeOnOffDto) {
        val probe = findById(dto.id)
        probe.enabled = dto.enabled
        probe.status = dto.status
        probe.persist()
    }
}
