package tmenier.fr.common.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import java.util.UUID

@RegisterForReflection
data class NotificationJob(
    val probeId: UUID,
    val result: ProbeResult,
    val previousStatus: ProbeMonitorLogStatus,
)
