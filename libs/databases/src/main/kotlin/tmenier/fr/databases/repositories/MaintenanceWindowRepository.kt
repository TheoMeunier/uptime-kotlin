package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.StoreMaintenanceWindowDto
import tmenier.fr.databases.entities.MaintenanceWindowEntity
import tmenier.fr.databases.mappers.MaintenanceMapper
import java.util.UUID

@ApplicationScoped
class MaintenanceWindowRepository(
    private val probeRepository: ProbeRepository,
    private val em: EntityManager,
) : PanacheRepositoryBase<MaintenanceWindowEntity, UUID> {
    override fun findById(id: UUID): MaintenanceWindowEntity = find("id = ?1", id).firstResult() ?: throw NotFoundException("Maintenance window not found: $id")

    fun findByIdOrNull(id: UUID): MaintenanceWindowEntity? = find("id = ?1", id).firstResult()

    fun getAll(): List<MaintenanceWindowEntity> =
        em
            .createQuery(
                """
                SELECT DISTINCT w
                FROM MaintenanceWindowEntity w
                LEFT JOIN FETCH w.probes
                ORDER BY w.startsAt DESC
                """.trimIndent(),
                MaintenanceWindowEntity::class.java,
            ).resultList

    fun findMaterialisable(): List<MaintenanceWindowEntity> =
        em
            .createQuery(
                """
                SELECT DISTINCT w
                FROM MaintenanceWindowEntity w
                JOIN FETCH w.probes
                WHERE w.active = true
                """.trimIndent(),
                MaintenanceWindowEntity::class.java,
            ).resultList

    fun findByProbe(probeId: UUID): List<MaintenanceWindowEntity> =
        em
            .createQuery(
                """
                SELECT DISTINCT w
                FROM MaintenanceWindowEntity w
                JOIN w.probes p
                WHERE p.id = :probeId
                ORDER BY w.startsAt DESC
                """.trimIndent(),
                MaintenanceWindowEntity::class.java,
            ).setParameter("probeId", probeId)
            .resultList

    fun save(
        dto: StoreMaintenanceWindowDto,
        probeIds: List<UUID>,
    ): MaintenanceWindowEntity {
        val entity = MaintenanceMapper.toEntity(dto)
        attach(probeIds, entity)
        entity.persist()

        return entity
    }

    fun update(
        dto: StoreMaintenanceWindowDto,
        probeIds: List<UUID>,
    ): MaintenanceWindowEntity {
        val entity = findById(dto.id)
        entity.title = dto.title
        entity.description = dto.description
        entity.startsAt = dto.startsAt
        entity.durationSeconds = dto.durationSeconds
        entity.recurrence = dto.recurrence
        entity.recurrenceUntil = dto.recurrenceUntil
        entity.timezone = dto.timezone
        entity.active = dto.active
        entity.isPublic = dto.isPublic

        entity.probes.clear()
        attach(probeIds, entity)
        entity.persist()

        return entity
    }

    fun attach(
        probeIds: List<UUID>,
        window: MaintenanceWindowEntity,
    ) {
        if (probeIds.isEmpty()) return

        window.probes.addAll(probeRepository.findByIds(probeIds))
    }

    fun delete(windowId: UUID) = delete("id = ?1", windowId)
}
