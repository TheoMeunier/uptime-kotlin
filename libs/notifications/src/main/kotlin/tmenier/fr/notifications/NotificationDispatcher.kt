package tmenier.fr.notifications

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.notifications.NotificationEvent
import tmenier.fr.databases.dtos.NotificationDto
import tmenier.fr.databases.dtos.ProbeDTO

@ApplicationScoped
class NotificationDispatcher(
    private val notificationFactory: NotificationFactory,
) {
    fun dispatch(
        notification: NotificationDto,
        probe: ProbeDTO,
        result: ProbeResult,
        event: NotificationEvent,
    ) {
        if (event == NotificationEvent.NONE) return

        val handler =
            notificationFactory.getNotification(notification.type)
                ?: throw IllegalArgumentException("Unknown notification handler type: ${notification.type}")

        @Suppress("UNCHECKED_CAST")
        val typedHandler = handler as TypedNotificationInterfaces<Any>

        when (event) {
            NotificationEvent.FAILURE -> typedHandler.sendFailure(notification.content, probe, result)
            NotificationEvent.RECOVERY -> typedHandler.sendSuccess(notification.content, probe, result)
            NotificationEvent.NONE -> Unit
        }
    }
}
