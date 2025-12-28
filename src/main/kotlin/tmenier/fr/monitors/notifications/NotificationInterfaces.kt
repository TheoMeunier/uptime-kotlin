package tmenier.fr.monitors.notifications

import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.schedulers.dto.ProbeResult

interface NotificationInterfaces {
    fun getNotificationType(): String
}

interface TypedNotificationInterfaces<T> : NotificationInterfaces {
    fun sendSuccess(
        content: T,
        probe: ProbesEntity,
        result: ProbeResult,
    )

    fun sendFailure(
        content: T,
        probe: ProbesEntity,
        result: ProbeResult,
    )

    fun sendTest(content: T)
}
