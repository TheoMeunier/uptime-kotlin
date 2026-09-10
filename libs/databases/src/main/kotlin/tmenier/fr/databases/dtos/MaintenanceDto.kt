package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.maintenances.MaintenanceRecurrence
import java.time.Instant
import java.util.UUID

@RegisterForReflection
data class MaintenanceOccurrenceDto(
    val id: UUID,
    val startsAt: Instant,
    val endsAt: Instant,
    val cancelled: Boolean,
)

@RegisterForReflection
data class MaintenanceWindowDto(
    val id: UUID,
    val title: String,
    val description: String?,
    val startsAt: Instant,
    val durationSeconds: Int,
    val recurrence: MaintenanceRecurrence,
    val recurrenceUntil: Instant?,
    val timezone: String,
    val active: Boolean,
    val isPublic: Boolean,
    val probes: List<ProbeListDTO>,
    val probeIds: List<UUID>,
    val currentOccurrence: MaintenanceOccurrenceDto? = null,
    val nextOccurrence: MaintenanceOccurrenceDto? = null,
    val upcomingOccurrences: List<MaintenanceOccurrenceDto> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
)

@RegisterForReflection
data class MaintenanceWindowListDto(
    val id: UUID,
    val title: String,
    val recurrence: MaintenanceRecurrence,
    val durationSeconds: Int,
    val timezone: String,
    val active: Boolean,
    val isPublic: Boolean,
    val probeCount: Int,
    val currentOccurrence: MaintenanceOccurrenceDto? = null,
    val nextOccurrence: MaintenanceOccurrenceDto? = null,
)

@RegisterForReflection
data class StoreMaintenanceWindowDto(
    val id: UUID,
    val title: String,
    val description: String?,
    val startsAt: Instant,
    val durationSeconds: Int,
    val recurrence: MaintenanceRecurrence,
    val recurrenceUntil: Instant?,
    val timezone: String,
    val active: Boolean,
    val isPublic: Boolean,
)

@RegisterForReflection
data class ProbeMaintenanceDto(
    val id: UUID,
    val windowId: UUID,
    val title: String,
    val startsAt: Instant,
    val endsAt: Instant,
)

@RegisterForReflection
data class ProbeMaintenanceState(
    val current: ProbeMaintenanceDto? = null,
    val next: ProbeMaintenanceDto? = null,
)
