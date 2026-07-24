package tmenier.fr.databases.dtos

import ProbeCheckTaskStatusEnum
import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.common.enums.notifications.NotificationEvent
import java.time.Instant
import java.util.UUID

@RegisterForReflection
data class NotificationQueueDispatcherDto(
    val probeId: UUID,
    val taskId: UUID,
    val result: ProbeResult,
    val status: ProbeMonitorLogStatus,
)

@RegisterForReflection
data class NotificationQueueRetryDto(
    val probeId: UUID,
    val taskId: UUID? = null,
    val notificationId: UUID? = null,
    val errorMessage: String,
    val channel: NotificationChannelsEnum,
    val event: NotificationEvent,
    val payload: ProbeResult,
)

@RegisterForReflection
data class NotificationQueueDto(
    val id: UUID,
    val probe: ProbeDTO,
    val notification: NotificationDto,
    val taskId: UUID? = null,
    val status: ProbeCheckTaskStatusEnum,
    val event: NotificationEvent,
    val payload: ProbeResult,
    val attemptCount: Int,
    val maxAttempts: Int,
)

@RegisterForReflection
data class UpdateNotificationQueueDto(
    val id: UUID,
    val status: String,
    val errorMessage: String,
    val attemptCount: Int,
    val maxAttempts: Int,
    val nextAttemptAt: Instant?,
)


@RegisterForReflection
data class RetryDecisionDto(
    val status: String,
    val attemptCount: Int,
    val nextAttemptAt: Instant?,
)
