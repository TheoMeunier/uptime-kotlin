package tmenier.fr.common.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.monitors.ProbeProtocol
import java.time.LocalDateTime
import java.util.UUID

@RegisterForReflection
data class ProbeJob(
    val probeId: UUID,
    val scheduledAt: LocalDateTime,
    val protocol: ProbeProtocol,
    val interval: Int,
    val retry: Int,
    val content: Any,
)
