package tmenier.fr.monitors.dtos.responses

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.monitors.enums.NotificationChannelsEnum
import tmenier.fr.monitors.notifications.dto.NotificationContent
import java.time.LocalDateTime
import java.util.UUID

@RegisterForReflection
data class ListingNotificationsDto(
    val id: UUID,
    val name: String,
    val isDefault: Boolean,
)

@RegisterForReflection
data class ShowNotificationsDto(
    val id: UUID,
    val name: String,
    val notificationType: NotificationChannelsEnum,
    val isDefault: Boolean,
    val content: NotificationContent,
    val createdAt: LocalDateTime,
)
