package tmenier.fr.notifications.dto

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.notifications.enums.NotificationChannelsEnum

@RegisterForReflection
data class NotificationTestingDto(
    val type: NotificationChannelsEnum,
    val content: NotificationContent,
)
