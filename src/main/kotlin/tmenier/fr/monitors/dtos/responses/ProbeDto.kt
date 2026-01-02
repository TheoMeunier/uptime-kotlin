package tmenier.fr.monitors.dtos.responses

import tmenier.fr.monitors.dtos.propbes.ProbeContent
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import java.time.LocalDateTime
import java.util.*

data class ProbeListDTO(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val status: ProbeMonitorLogStatus,
)

data class ProbeShowDTO(
    val probe: ProbeDTO,
    val monitors: List<ProbeMonitorDTO>,
)

data class ProbeStatusDTO(
    val probe: ProbeListDTO,
    val monitors: List<ProbeMonitorDTO>,
)

data class ProbeDTO(
    val id: UUID,
    val name: String,
    val interval: Int,
    val timeout: Int,
    val retry: Int,
    val intervalRetry: Int,
    val enabled: Boolean,
    val protocol: String,
    val description: String?,
    val lastRun: LocalDateTime?,
    val status: ProbeMonitorLogStatus,
    val content: ProbeContent,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
