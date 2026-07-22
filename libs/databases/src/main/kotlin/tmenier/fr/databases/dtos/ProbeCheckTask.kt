package tmenier.fr.databases.dtos

import ProbeCheckTaskStatusEnum
import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID


@RegisterForReflection
data class ProbeCheckTaskDto(
    val id: UUID,
    val probeId: UUID,
    val region: String,
    val attemptNumber: Int,
    val status: ProbeCheckTaskStatusEnum,
    val scheduledAt: LocalDateTime,
    val claimedBy: String? = null,
    val claimedAt: Instant? = null,
    val resultMessage: String? = null,
    val createdAt: LocalDateTime,
)

@RegisterForReflection
data class StoreProbeCheckTaskDto(
    val probeId: UUID,
    val region: String,
    val attemptNumber: Int,
    val scheduleAt: LocalDateTime,
)
