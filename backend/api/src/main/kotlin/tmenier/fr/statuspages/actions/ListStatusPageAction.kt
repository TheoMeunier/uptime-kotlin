package tmenier.fr.statuspages.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.ProbeStatusDTO
import tmenier.fr.databases.dtos.PublicStatusPageDto
import tmenier.fr.databases.dtos.PublicStatusPageGroupDto
import tmenier.fr.databases.dtos.StatusPageDto
import tmenier.fr.databases.dtos.StatusPageGroupDto
import tmenier.fr.databases.dtos.StatusPageListItemDto
import tmenier.fr.databases.entities.StatusPageEntity
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.StatusPageRepository
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class ListStatusPageAction(
    private val statusPageRepository: StatusPageRepository,
) {
    @Transactional
    fun execute(): List<StatusPageListItemDto> =
        statusPageRepository.getAll().map { page ->
            StatusPageListItemDto(
                id = page.id,
                slug = page.slug,
                title = page.title,
                description = page.description,
                defaultLayout = page.defaultLayout,
                groupCount = page.groups.size,
                probeCount = page.groups.sumOf { it.orderedProbes().size },
                updatedAt = page.updatedAt,
            )
        }

    @Transactional
    fun show(statusPageId: UUID): StatusPageDto {
        val page = statusPageRepository.findById(statusPageId)

        return StatusPageDto(
            id = page.id,
            slug = page.slug,
            title = page.title,
            description = page.description,
            defaultLayout = page.defaultLayout,
            groups =
                page.groups.map { group ->
                    StatusPageGroupDto(
                        name = group.name,
                        probes = group.orderedProbes().map { ProbeMapper.toProbeListDto(it) },
                    )
                },
            createdAt = page.createdAt,
            updatedAt = page.updatedAt,
        )
    }

    @Transactional
    fun version(slug: String): Instant = findPublished(slug).updatedAt

    @Transactional
    fun arrange(
        slug: String,
        snapshot: (Set<UUID>) -> List<ProbeStatusDTO>,
    ): PublicStatusPageDto {
        val page = findPublished(slug)
        val probeIds = page.groups.flatMap { it.orderedProbes() }.mapTo(mutableSetOf()) { it.id }
        val byProbe = snapshot(probeIds).associateBy { it.probe.id }

        return PublicStatusPageDto(
            slug = page.slug,
            title = page.title,
            description = page.description,
            defaultLayout = page.defaultLayout,
            groups =
                page.groups.map { group ->
                    PublicStatusPageGroupDto(
                        name = group.name,
                        items = group.orderedProbes().mapNotNull { byProbe[it.id] },
                    )
                },
        )
    }

    private fun findPublished(slug: String): StatusPageEntity = statusPageRepository.findBySlug(slug) ?: throw NotFoundException("Status page not found: $slug")
}
