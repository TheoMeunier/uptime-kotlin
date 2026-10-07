package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.StoreStatusPageDto
import tmenier.fr.databases.dtos.StoreStatusPageGroupDto
import tmenier.fr.databases.entities.StatusPageEntity
import tmenier.fr.databases.entities.StatusPageGroupEntity
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class StatusPageRepository(
    private val probeRepository: ProbeRepository,
) : PanacheRepositoryBase<StatusPageEntity, UUID> {
    override fun findById(id: UUID): StatusPageEntity = find("id = ?1", id).firstResult() ?: throw NotFoundException("Status page not found: $id")

    fun findBySlug(slug: String): StatusPageEntity? = find("slug = ?1", slug).firstResult()

    fun getAll(): List<StatusPageEntity> = findAll(Sort.by("title")).list()

    fun isSlugTaken(
        slug: String,
        excludingId: UUID?,
    ): Boolean =
        if (excludingId == null) {
            count("slug = ?1", slug) > 0
        } else {
            count("slug = ?1 and id <> ?2", slug, excludingId) > 0
        }

    fun save(dto: StoreStatusPageDto): StatusPageEntity {
        val entity =
            StatusPageEntity().apply {
                id = dto.id
                slug = dto.slug
                title = dto.title
                description = dto.description
                defaultLayout = dto.defaultLayout
            }

        replaceGroups(entity, dto.groups)
        entity.persist()

        return entity
    }

    fun update(dto: StoreStatusPageDto): StatusPageEntity {
        val entity = findById(dto.id)
        entity.slug = dto.slug
        entity.title = dto.title
        entity.description = dto.description
        entity.defaultLayout = dto.defaultLayout
        entity.updatedAt = Instant.now()

        replaceGroups(entity, dto.groups)
        entity.persist()

        return entity
    }

    private fun replaceGroups(
        page: StatusPageEntity,
        groups: List<StoreStatusPageGroupDto>,
    ) {
        page.groups.clear()

        val probeIds = groups.flatMap { it.probeIds }.distinct()
        val probesById =
            if (probeIds.isEmpty()) emptyMap() else probeRepository.findByIds(probeIds).associateBy { it.id }

        groups.forEachIndexed { index, group ->
            page.groups.add(
                StatusPageGroupEntity().apply {
                    id = UUID.randomUUID()
                    statusPage = page
                    name = group.name
                    position = index
                    probes.addAll(group.probeIds.mapNotNull { probesById[it] })
                },
            )
        }
    }

    fun delete(id: UUID) = delete("id = ?1", id)
}
