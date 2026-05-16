package tmenier.fr.common.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.LocalDateTime
import java.util.UUID

@RegisterForReflection
data class ProbeJob(
    val probeId: UUID,
    val scheduledAt: LocalDateTime,
)
