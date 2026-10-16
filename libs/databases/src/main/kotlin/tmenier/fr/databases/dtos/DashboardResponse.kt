package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.incidents.IncidentClosedReason
import tmenier.fr.common.enums.incidents.IncidentEventType
import java.time.Instant
import java.util.UUID

@RegisterForReflection
data class MonitorSummary(
    val totalMonitors: Long,
    val totalMonitorsSuccess: Long,
    val totalMonitorsFailures: Long,
    val avgUptimePercent: Double,
)

@RegisterForReflection
data class ResponseMetrics24h(
    val avgResponseTimeMs: Double,
    val countChecks24h: Long,
)

@RegisterForReflection
data class SparklinePoint(
    val bucket: Instant,
    val value: Double,
)

/**
 * The opening or the closing of an incident. An opening is dated at the first failed check
 * ([IncidentDto.startedAt]), like on the incidents page, not at the alert.
 */
@RegisterForReflection
data class IncidentEventDto(
    val incidentId: UUID,
    val probeId: UUID,
    val probeName: String,
    val type: IncidentEventType,
    val at: Instant,
    val message: String,
    val ongoing: Boolean,
    val durationSeconds: Long?,
    val closedReason: IncidentClosedReason?,
)

@RegisterForReflection
data class IncidentBar(
    val hour: Instant,
    val upCount: Long,
    val downCount: Long,
)
