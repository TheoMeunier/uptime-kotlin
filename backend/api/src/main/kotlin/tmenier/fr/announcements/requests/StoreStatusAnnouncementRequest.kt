package tmenier.fr.announcements.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import tmenier.fr.common.enums.statuspages.AnnouncementSeverity
import java.time.Instant
import java.util.UUID

@RegisterForReflection
class StoreStatusAnnouncementRequest {
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must be at most 255 characters")
    lateinit var title: String

    @field:Size(max = 5000, message = "Text must be at most 5000 characters")
    var body: String? = null

    @field:NotNull(message = "Severity is required")
    var severity: AnnouncementSeverity = AnnouncementSeverity.INFO

    var startsAt: Instant? = null

    var endsAt: Instant? = null

    var showOnGlobal: Boolean = false

    @field:Size(max = 100, message = "An announcement targets at most 100 status pages")
    var statusPageIds: List<UUID> = emptyList()
}
