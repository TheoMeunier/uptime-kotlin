package tmenier.fr.notifications

import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.databases.dtos.ProbeDTO

interface NotificationInterfaces {
    fun getNotificationType(): String
}

interface TypedNotificationInterfaces<T> : NotificationInterfaces {
    fun sendSuccess(
        content: T,
        probe: ProbeDTO,
        result: ProbeResult,
    )

    fun sendFailure(
        content: T,
        probe: ProbeDTO,
        result: ProbeResult,
    )

    fun sendReminder(
        content: T,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) = sendFailure(content, probe, result)

    fun sendTest(content: T)
}
