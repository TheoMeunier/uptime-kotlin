package tmenier.fr.announcements.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.statuspages.AnnouncementState
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.databases.dtos.StatusAnnouncementDto
import tmenier.fr.databases.repositories.StatusAnnouncementRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@ApplicationScoped
class EndStatusAnnouncementAction(
    private val statusAnnouncementRepository: StatusAnnouncementRepository,
) {
    @Transactional
    fun execute(
        announcementId: UUID,
        now: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS),
    ): StatusAnnouncementDto {
        statusAnnouncementRepository.end(announcementId, now)

        val announcement = statusAnnouncementRepository.findById(announcementId)

        if (announcement.stateAt(now) == AnnouncementState.SCHEDULED) {
            throw ApiException(
                ANNOUNCEMENT_NOT_STARTED,
                "A scheduled announcement cannot be ended; edit or delete it instead.",
                Response.Status.CONFLICT,
            )
        }

        return ListStatusAnnouncementAction.toDto(announcement, now)
    }

    companion object {
        const val ANNOUNCEMENT_NOT_STARTED = "ANNOUNCEMENT_NOT_STARTED"
    }
}
