package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.probes.QueueJobStatus
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@RegisterForReflection
data class ProbeCheckTaskDto(
    val id: UUID,
    val probeId: UUID,
    val region: String,
    val attemptNumber: Int,
    val status: QueueJobStatus,
    val scheduledAt: LocalDateTime,
    val availableAt: Instant,
    val claimedBy: String? = null,
    val leaseUntil: Instant? = null,
    val deliveryAttempts: Int = 0,
    val maxDeliveryAttempts: Int = 5,
    val previousFailedAt: Instant? = null,
    val resultMessage: String? = null,
    val createdAt: LocalDateTime,
)

@RegisterForReflection
data class StoreProbeCheckTaskDto(
    val probeId: UUID,
    val region: String,
    val attemptNumber: Int,
    val scheduleAt: LocalDateTime,
    val availableAt: Instant,
    val previousFailedAt: Instant? = null,
)
