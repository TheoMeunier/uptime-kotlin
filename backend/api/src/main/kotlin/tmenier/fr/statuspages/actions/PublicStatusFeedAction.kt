package tmenier.fr.statuspages.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.PublicAnnouncementDto
import tmenier.fr.databases.dtos.PublicStatusFeedDto
import tmenier.fr.databases.entities.StatusAnnouncementEntity
import tmenier.fr.databases.repositories.StatusAnnouncementRepository
import tmenier.fr.databases.repositories.StatusPageRepository
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class PublicStatusFeedAction(
    private val statusPageRepository: StatusPageRepository,
    private val statusAnnouncementRepository: StatusAnnouncementRepository,
) {
    @Transactional
    fun forPage(
        slug: String,
        now: Instant = Instant.now(),
    ): PublicStatusFeedDto {
        val page = statusPageRepository.findBySlug(slug) ?: throw NotFoundException("Status page not found: $slug")
        return build(page.id, now)
    }

    @Transactional
    fun forGlobal(now: Instant = Instant.now()): PublicStatusFeedDto = build(statusPageId = null, now = now)

    private fun build(
        statusPageId: UUID?,
        now: Instant,
    ) = PublicStatusFeedDto(
        announcements = statusAnnouncementRepository.findActive(statusPageId, now).map(::toPublic),
    )

    private fun toPublic(entity: StatusAnnouncementEntity) =
        PublicAnnouncementDto(
            id = entity.id,
            title = entity.title,
            body = entity.body,
            severity = entity.severity,
            startsAt = entity.startsAt,
            endsAt = entity.endsAt,
            updatedAt = entity.updatedAt,
        )
}
