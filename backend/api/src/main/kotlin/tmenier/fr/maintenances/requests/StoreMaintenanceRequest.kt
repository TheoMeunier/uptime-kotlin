package tmenier.fr.maintenances.requests

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import tmenier.fr.common.enums.maintenances.MaintenanceRecurrence
import java.time.Instant
import java.util.UUID

@RegisterForReflection
class StoreMaintenanceRequest {
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must be at most 255 characters")
    lateinit var title: String

    var description: String? = null

    @field:NotNull(message = "Start date is required")
    lateinit var startsAt: Instant

    @field:Min(value = 60, message = "Duration must be at least 60 seconds")
    var durationSeconds: Int = 0

    @field:NotNull(message = "Recurrence is required")
    var recurrence: MaintenanceRecurrence = MaintenanceRecurrence.ONCE

    var recurrenceUntil: Instant? = null

    @field:NotBlank(message = "Timezone is required")
    var timezone: String = "UTC"

    var active: Boolean = true

    var isPublic: Boolean = true

    var probeIds: List<UUID> = emptyList()
}

@RegisterForReflection
class StartAdHocMaintenanceRequest {
    @field:Min(value = 1, message = "Duration must be at least 1 minute")
    var durationMinutes: Int = 60

    var title: String? = null
}
