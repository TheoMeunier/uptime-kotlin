package tmenier.fr.announcements.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.announcements.requests.StoreStatusAnnouncementRequest
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.databases.dtos.StoreStatusAnnouncementDto
import tmenier.fr.databases.repositories.StatusAnnouncementRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@ApplicationScoped
class StoreStatusAnnouncementAction(
    private val statusAnnouncementRepository: StatusAnnouncementRepository,
) {
    @Transactional
    fun execute(
        payload: StoreStatusAnnouncementRequest,
        announcementId: UUID? = null,
        now: Instant = Instant.now(),
    ): UUID {
        val current = announcementId?.let(statusAnnouncementRepository::findById)
        val startsAt = (payload.startsAt ?: current?.startsAt ?: now).truncatedTo(ChronoUnit.SECONDS)
        val endsAt = payload.endsAt?.truncatedTo(ChronoUnit.SECONDS)

        if (endsAt != null && !endsAt.isAfter(startsAt)) {
            throw BadRequestException("The end of an announcement must come after its start")
        }

        val pageIds = payload.statusPageIds.toSet()
        if (!payload.showOnGlobal && pageIds.isEmpty()) {
            throw BadRequestException("Choose at least one status page for the announcement")
        }

        val dto =
            StoreStatusAnnouncementDto(
                id = announcementId ?: UUID.randomUUID(),
                title = payload.title.trim(),
                body = payload.body?.trim()?.ifEmpty { null },
                severity = payload.severity,
                startsAt = startsAt,
                endsAt = endsAt,
                showOnGlobal = payload.showOnGlobal,
                statusPageIds = pageIds,
            )

        if (current != null) statusAnnouncementRepository.update(dto) else statusAnnouncementRepository.save(dto)

        return dto.id
    }
}
