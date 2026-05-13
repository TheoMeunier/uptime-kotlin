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

    fun sendTest(content: T)
}
