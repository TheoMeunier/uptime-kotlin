package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import java.time.LocalDateTime
import java.util.UUID

@RegisterForReflection
data class ProbeMonitorDTO(
    val id: UUID,
    val status: ProbeMonitorLogStatus,
    val responseTime: Long,
    val message: String,
    val runAt: LocalDateTime,
)

@RegisterForReflection
data class StoreProbeMonitorLogDto(
    val runAt: LocalDateTime,
    val message: String,
    val status: ProbeMonitorLogStatus,
    val responseTime: Long,
    val probe: ProbeDTO,
)
