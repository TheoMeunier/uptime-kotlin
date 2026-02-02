package tmenier.fr.dashboard.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
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
    val countIncidents24h: Long,
    val countChecks24h: Long,
)

@RegisterForReflection
data class DownProbeDto(
    val id: UUID,
    val name: String,
    val downDuration: String,
)
