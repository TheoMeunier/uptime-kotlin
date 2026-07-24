package tmenier.fr.notifications.dto

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent

@RegisterForReflection
data class NotificationTestingDto(
    val type: NotificationChannelsEnum,
    val content: NotificationContent,
)

@RegisterForReflection
sealed class NotificationTestResult {
    object Success : NotificationTestResult()
    data class Failure(val message: String) : NotificationTestResult()
}
