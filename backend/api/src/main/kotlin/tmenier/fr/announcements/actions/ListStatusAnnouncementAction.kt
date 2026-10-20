package tmenier.fr.announcements.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.statuspages.AnnouncementState
import tmenier.fr.databases.dtos.AnnouncementPageRefDto
import tmenier.fr.databases.dtos.StatusAnnouncementDto
import tmenier.fr.databases.entities.StatusAnnouncementEntity
import tmenier.fr.databases.repositories.StatusAnnouncementRepository
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class ListStatusAnnouncementAction(
    private val statusAnnouncementRepository: StatusAnnouncementRepository,
) {
    @Transactional
    fun execute(now: Instant = Instant.now()): List<StatusAnnouncementDto> =
        statusAnnouncementRepository
            .getAll()
            .map { toDto(it, now) }
            .sortedWith(
                compareBy<StatusAnnouncementDto> { STATE_ORDER.getValue(it.state) }
                    .thenBy { if (it.state == AnnouncementState.SCHEDULED) it.startsAt else null }
                    .thenByDescending { it.endsAt ?: it.startsAt },
            )

    @Transactional
    fun show(
        announcementId: UUID,
        now: Instant = Instant.now(),
    ): StatusAnnouncementDto = toDto(statusAnnouncementRepository.findById(announcementId), now)

    companion object {
        private val STATE_ORDER =
            mapOf(
                AnnouncementState.ACTIVE to 0,
                AnnouncementState.SCHEDULED to 1,
                AnnouncementState.ENDED to 2,
            )

        fun toDto(
            entity: StatusAnnouncementEntity,
            now: Instant,
        ): StatusAnnouncementDto =
            StatusAnnouncementDto(
                id = entity.id,
                title = entity.title,
                body = entity.body,
                severity = entity.severity,
                state = entity.stateAt(now),
                startsAt = entity.startsAt,
                endsAt = entity.endsAt,
                showOnGlobal = entity.showOnGlobal,
                pages =
                    entity.pages
                        .sortedBy { it.title.lowercase() }
                        .map { AnnouncementPageRefDto(id = it.id, slug = it.slug, title = it.title) },
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
    }
}
