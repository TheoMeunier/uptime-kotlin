package tmenier.fr.monitors.schedulers.dto

import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import java.time.LocalDateTime

data class ProbeResult(
    val status: ProbeMonitorLogStatus,
    val responseTime: Long,
    val message: String,
    val runAt: LocalDateTime,
    val statusCode: Int? = null,
    val responseBody: String? = null
)