package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.StoreStatusAnnouncementDto
import tmenier.fr.databases.entities.StatusAnnouncementEntity
import tmenier.fr.databases.entities.StatusPageEntity
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class StatusAnnouncementRepository(
    private val em: EntityManager,
) : PanacheRepositoryBase<StatusAnnouncementEntity, UUID> {
    override fun findById(id: UUID): StatusAnnouncementEntity = find("id = ?1", id).firstResult() ?: throw NotFoundException("Announcement not found: $id")

    fun getAll(): List<StatusAnnouncementEntity> =
        em
            .createQuery(
                "SELECT DISTINCT a FROM StatusAnnouncementEntity a LEFT JOIN FETCH a.pages ORDER BY a.startsAt DESC, a.id",
                StatusAnnouncementEntity::class.java,
            ).resultList

    fun findActive(
        statusPageId: UUID?,
        now: Instant,
        limit: Int = ACTIVE_LIMIT,
    ): List<StatusAnnouncementEntity> {
        val target =
            if (statusPageId == null) {
                "a.showOnGlobal = TRUE"
            } else {
                "EXISTS (SELECT 1 FROM StatusAnnouncementEntity b JOIN b.pages p WHERE b.id = a.id AND p.id = :pageId)"
            }

        val query =
            em
                .createQuery(
                    """
                    SELECT a FROM StatusAnnouncementEntity a
                    WHERE $target
                      AND a.startsAt <= :now
                      AND (a.endsAt IS NULL OR a.endsAt > :now)
                    ORDER BY a.startsAt DESC, a.id
                    """.trimIndent(),
                    StatusAnnouncementEntity::class.java,
                ).setParameter("now", now)
                .setMaxResults(limit)

        statusPageId?.let { query.setParameter("pageId", it) }

        return query.resultList
    }

    fun save(dto: StoreStatusAnnouncementDto): StatusAnnouncementEntity {
        val entity = StatusAnnouncementEntity().apply { id = dto.id }
        apply(entity, dto)
        persist(entity)
        return entity
    }

    fun update(dto: StoreStatusAnnouncementDto): StatusAnnouncementEntity {
        val entity = findById(dto.id)
        apply(entity, dto)
        entity.updatedAt = Instant.now()
        return entity
    }

    fun end(
        id: UUID,
        at: Instant,
    ): Boolean =
        em
            .createQuery(
                """
                UPDATE StatusAnnouncementEntity a
                SET a.endsAt = :at, a.updatedAt = :at
                WHERE a.id = :id AND a.startsAt <= :at AND (a.endsAt IS NULL OR a.endsAt > :at)
                """.trimIndent(),
            ).setParameter("at", at)
            .setParameter("id", id)
            .executeUpdate() == 1

    fun delete(id: UUID) = findById(id).delete()

    private fun apply(
        entity: StatusAnnouncementEntity,
        dto: StoreStatusAnnouncementDto,
    ) {
        entity.title = dto.title
        entity.body = dto.body
        entity.severity = dto.severity
        entity.startsAt = dto.startsAt
        entity.endsAt = dto.endsAt
        entity.showOnGlobal = dto.showOnGlobal

        val pages =
            if (dto.statusPageIds.isEmpty()) {
                emptyList()
            } else {
                em
                    .createQuery("SELECT p FROM StatusPageEntity p WHERE p.id IN :ids", StatusPageEntity::class.java)
                    .setParameter("ids", dto.statusPageIds)
                    .resultList
            }

        if (pages.size != dto.statusPageIds.size) {
            throw BadRequestException("Unknown status page in ${dto.statusPageIds}")
        }

        entity.pages.clear()
        entity.pages.addAll(pages)
    }

    companion object {
        const val ACTIVE_LIMIT = 20
    }
}
