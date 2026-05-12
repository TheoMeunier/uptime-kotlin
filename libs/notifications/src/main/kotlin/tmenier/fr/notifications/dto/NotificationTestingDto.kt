package tmenier.fr.notifications.dto

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent

@RegisterForReflection
data class NotificationTestingDto(
    val type: NotificationChannelsEnum,
    val content: NotificationContent,
)
