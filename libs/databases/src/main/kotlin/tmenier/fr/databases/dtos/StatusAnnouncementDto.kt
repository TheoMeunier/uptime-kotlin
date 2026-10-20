package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.statuspages.AnnouncementSeverity
import tmenier.fr.common.enums.statuspages.AnnouncementState
import java.time.Instant
import java.util.UUID

@RegisterForReflection
data class StoreStatusAnnouncementDto(
    val id: UUID,
    val title: String,
    val body: String?,
    val severity: AnnouncementSeverity,
    val startsAt: Instant,
    val endsAt: Instant?,
    val showOnGlobal: Boolean,
    val statusPageIds: Set<UUID>,
)

@RegisterForReflection
data class AnnouncementPageRefDto(
    val id: UUID,
    val slug: String,
    val title: String,
)

@RegisterForReflection
data class StatusAnnouncementDto(
    val id: UUID,
    val title: String,
    val body: String?,
    val severity: AnnouncementSeverity,
    val state: AnnouncementState,
    val startsAt: Instant,
    val endsAt: Instant?,
    val showOnGlobal: Boolean,
    val pages: List<AnnouncementPageRefDto>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@RegisterForReflection
data class PublicAnnouncementDto(
    val id: UUID,
    val title: String,
    val body: String?,
    val severity: AnnouncementSeverity,
    val startsAt: Instant,
    val endsAt: Instant?,
    val updatedAt: Instant,
)

@RegisterForReflection
data class PublicStatusFeedDto(
    val announcements: List<PublicAnnouncementDto>,
)
