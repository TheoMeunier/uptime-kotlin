package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.dtos.ProbeOnOffDto
import tmenier.fr.databases.dtos.ProbeStatusDTO
import tmenier.fr.databases.dtos.StoreProbeDto
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.ProbeContentMapper
import tmenier.fr.databases.mappers.ProbeMapper
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class ProbeRepository(
    private val notificationRepository: NotificationRepository,
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
    private val em: EntityManager,
) : PanacheRepositoryBase<ProbesEntity, UUID> {
    @Transactional
    override fun findById(id: UUID): ProbesEntity = find("id = ?1", id).firstResult() ?: throw IllegalArgumentException("Probe not found")

    fun findByIdOrNull(id: UUID): ProbesEntity? = find("id = ?1", id).firstResult()

    fun findByIdForUpdate(id: UUID): ProbesEntity =
        em.find(ProbesEntity::class.java, id, LockModeType.PESSIMISTIC_WRITE)
            ?: throw IllegalArgumentException("Probe not found")

    fun findByIds(ids: List<UUID>): List<ProbesEntity> = find("id in ?1", ids).list()

    fun getProbesLastHour(): List<ProbeStatusDTO> =
        find(
            "SELECT DISTINCT p FROM ProbesEntity p JOIN FETCH p.probesMonitorLogs pml WHERE pml.runAt > ?1 AND p.enabled = true ORDER BY p.name ASC",
            LocalDateTime.now().minusHours(1),
        ).list().sortedBy { it.name.lowercase() }.map { ProbeMapper.toStatusDto(it) }

    fun getActiveProbes(): List<ProbeDTO> = find("enabled = ?1 ORDER BY name ASC", true).list().map { ProbeMapper.toDto(it) }

    fun getAll(): List<ProbesEntity> = findAll(Sort.by("name")).list()

    fun attach(
        notifications: List<UUID>,
        probe: ProbesEntity,
    ) {
        val notificationsEntities = notificationRepository.findByIds(notifications)
        probe.notifications.addAll(notificationsEntities)
    }

    fun delete(probeId: UUID) = delete("id = ?1", probeId)

    fun save(
        dto: StoreProbeDto,
        notifications: List<UUID>,
    ) {
        val entity = ProbeMapper.toEntity(dto)
        attach(notifications, entity)
        entity.persist()
    }

    fun update(
        dto: StoreProbeDto,
        notifications: List<UUID>,
    ) {
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
        probeCheckTaskRepository.reschedulePendingRetries(dto.id, dto.intervalRetry)
    }

    fun updateStatus(
        probeId: UUID,
        status: ProbeMonitorLogStatus,
    ) {
        val entity = findById(probeId)
        entity.status = status
        entity.updatedAt = LocalDateTime.now()
        entity.persist()
    }

    fun onOff(dto: ProbeOnOffDto) {
        val probe = findByIdForUpdate(dto.id)
        probe.enabled = dto.enabled
        probe.status = dto.status
        probe.nextCheckAt = if (dto.enabled) LocalDateTime.now() else null
        if (!dto.enabled) {
            probeCheckTaskRepository.cancelPending(dto.id)
        }
        probe.persist()
    }
}
