package tmenier.fr.databases.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import java.time.LocalDateTime
import java.util.UUID

@RegisterForReflection
data class NotificationDto(
    val id: UUID,
    val name: String,
    val type: NotificationChannelsEnum,
    val isDefault: Boolean,
    val content: NotificationContent,
)

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
