package tmenier.fr.monitors.notifications.dto

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.monitors.enums.NotificationChannelsEnum

@RegisterForReflection
data class NotificationTestingDto(
    val type: NotificationChannelsEnum,
    val content: NotificationContent,
)
