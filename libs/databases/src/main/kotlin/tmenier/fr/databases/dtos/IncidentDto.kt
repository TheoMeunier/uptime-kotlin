package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.incidents.IncidentClosedReason
import tmenier.fr.common.enums.incidents.IncidentState
import java.time.Instant
import java.util.UUID

@RegisterForReflection
data class IncidentDto(
    val id: UUID,
    val probeId: UUID,
    val probeName: String,
    val startedAt: Instant,
    val confirmedAt: Instant,
    val endedAt: Instant?,
    val durationSeconds: Long?,
    val openingMessage: String,
    val closedReason: IncidentClosedReason?,
)

@RegisterForReflection
data class IncidentPeriodDto(
    val id: UUID,
    val startedAt: Instant,
    val endedAt: Instant?,
)

@RegisterForReflection
data class IncidentPageDto(
    val items: List<IncidentDto>,
    val total: Long,
    val page: Int,
    val size: Int,
)

@RegisterForReflection
data class IncidentSummaryDto(
    val from: Instant,
    val to: Instant,
    val count: Long,
    val ongoing: Long,
    val totalDowntimeSeconds: Long,
    val mttrSeconds: Long?,
    val longest: IncidentDto?,
)

data class IncidentFilter(
    val state: IncidentState? = null,
    val probeId: UUID? = null,
    val from: Instant? = null,
    val to: Instant? = null,
)
