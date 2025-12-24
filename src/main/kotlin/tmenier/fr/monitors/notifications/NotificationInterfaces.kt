package tmenier.fr.monitors.notifications

import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.notifications.dto.NotificationContent
import tmenier.fr.monitors.schedulers.dto.ProbeResult

interface NotificationInterfaces {
    fun sendSuccess(content: NotificationContent, probe: ProbesEntity, result: ProbeResult)
    fun sendFailure(content: NotificationContent, probe: ProbesEntity, result: ProbeResult)
    fun sendTest(content: NotificationContent)
    fun getNotificationType(): String
}

interface TypedNotificationInterfaces<T : NotificationContent> {

    fun sendSuccess(content: T, probe: ProbesEntity, result: ProbeResult);

    fun sendFailure(content: T, probe: ProbesEntity, result: ProbeResult);

    fun sendTest(content: T);

    fun getNotificationType(): String
}