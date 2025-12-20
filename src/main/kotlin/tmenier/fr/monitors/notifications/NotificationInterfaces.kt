package tmenier.fr.monitors.notifications

import tmenier.fr.monitors.dtos.responses.ProbeMonitorDTO
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.notifications.dto.NotificationContent

interface NotificationInterfaces {
    fun sendSuccess(content: NotificationContent, probe: ProbesEntity, result: ProbeMonitorDTO)
    fun sendFailure(content: NotificationContent, probe: ProbesEntity, result: ProbeMonitorDTO)
    fun sendTest(content: NotificationContent)
    fun getNotificationType(): String
}

interface TypedNotificationInterfaces<T : NotificationContent> {

    fun sendSuccess(content: T, probe: ProbesEntity, result: ProbeMonitorDTO);

    fun sendFailure(content: T, probe: ProbesEntity, result: ProbeMonitorDTO);

    fun sendTest(content: T);

    fun getNotificationType(): String
}