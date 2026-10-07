package tmenier.fr.statuspages.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.databases.dtos.StoreStatusPageDto
import tmenier.fr.databases.dtos.StoreStatusPageGroupDto
import tmenier.fr.databases.repositories.StatusPageRepository
import tmenier.fr.statuspages.requests.StoreStatusPageRequest
import java.util.UUID

@ApplicationScoped
class StoreStatusPageAction(
    private val statusPageRepository: StatusPageRepository,
) {
    @Transactional
    fun execute(
        payload: StoreStatusPageRequest,
        statusPageId: UUID? = null,
    ): UUID {
        val slug = payload.slug.trim()
        validate(payload, slug, statusPageId)

        val dto =
            StoreStatusPageDto(
                id = statusPageId ?: UUID.randomUUID(),
                slug = slug,
                title = payload.title.trim(),
                description = payload.description?.trim()?.ifEmpty { null },
                defaultLayout = payload.defaultLayout,
                groups =
                    payload.groups.map { group ->
                        StoreStatusPageGroupDto(
                            name = group.name?.trim()?.ifEmpty { null },
                            probeIds = group.probeIds,
                        )
                    },
            )

        if (statusPageId != null) statusPageRepository.update(dto) else statusPageRepository.save(dto)

        return dto.id
    }

    private fun validate(
        payload: StoreStatusPageRequest,
        slug: String,
        statusPageId: UUID?,
    ) {
        if (statusPageRepository.isSlugTaken(slug, excludingId = statusPageId)) {
            throw BadRequestException("The address /status/$slug is already used by another status page")
        }

        val probeIds = payload.groups.flatMap { it.probeIds }
        if (probeIds.size != probeIds.toSet().size) {
            throw BadRequestException("A monitor can only appear once on a status page")
        }
    }
}
