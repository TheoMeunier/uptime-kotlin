package tmenier.fr.monitors.dtos.responses

import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import java.time.LocalDateTime
import java.util.UUID

data class ProbeMonitorDTO(
    val id: UUID,
    val status: ProbeMonitorLogStatus,
    val responseTime: Long,
    val message: String,
    val runAt: LocalDateTime,
)
