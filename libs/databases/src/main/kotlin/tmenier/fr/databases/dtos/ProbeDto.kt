package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import java.time.LocalDateTime
import java.util.UUID

@RegisterForReflection
data class ProbeListDTO(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val url: String? = null,
    val status: ProbeMonitorLogStatus,
)

@RegisterForReflection
data class ProbeShowDTO(
    val probe: ProbeDTO,
    val monitors: List<ProbeMonitorDTO>,
    val uptimes: ProbeUptimeDTO? = null,
)

@RegisterForReflection
data class ProbeStatusDTO(
    val probe: ProbeListDTO,
    val monitors: List<ProbeMonitorDTO>,
)

@RegisterForReflection
data class ProbeWithNotificationsDTO(
    val probe: ProbeDTO,
    val notifications: List<NotificationDto>,
)

@RegisterForReflection
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
    val url: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

@RegisterForReflection
data class StoreProbeDto(
    val id: UUID,
    val name: String,
    val interval: Int,
    val intervalRetry: Int,
    val retry: Int,
    val protocol: ProbeProtocol,
    val enabled: Boolean,
    val description: String?,
    val content: ProbeContent,
)

@RegisterForReflection
data class ProbeOnOffDto(
    val id: UUID,
    val enabled: Boolean,
    val status: ProbeMonitorLogStatus,
)

@RegisterForReflection
data class ProbeUptimeDTO(
    val h24: Double,
    val d7: Double,
    val d30: Double,
)
