package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.dtos.ProbeStatusDTO
import tmenier.fr.databases.entities.NotificationsChannelEntity
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.ProbeMapper
import java.util.UUID

@ApplicationScoped
class ProbeRepository {

    fun findById(probeId: UUID): ProbesEntity = ProbesEntity.findById(probeId) ?: throw IllegalArgumentException("Probe not found")

    fun findByIdWithLogs(probeId: UUID, hour: Long = 1) = ProbesEntity.findByIdWithLogs(probeId, hour) ?: throw IllegalArgumentException("Probe not found")

    fun getProbesLastHour(): List<ProbeStatusDTO> = ProbesEntity.getProbesLastHour().map { ProbeMapper.toStatusDto(it) }

    fun getAll(): List<ProbesEntity> = ProbesEntity.getAllProbes()

    fun delete(probeId: UUID) = ProbesEntity.delete(probeId)

    fun attach(notifications: List<NotificationsChannelEntity>, probe: ProbesEntity) = probe.notifications.addAll(notifications)

}
