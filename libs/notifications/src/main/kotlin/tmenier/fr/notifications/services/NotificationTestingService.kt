package tmenier.fr.notifications.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.logger
import tmenier.fr.notifications.NotificationFactory
import tmenier.fr.notifications.TypedNotificationInterfaces
import tmenier.fr.notifications.dto.NotificationTestResult
import tmenier.fr.notifications.dto.NotificationTestingDto

@ApplicationScoped
class NotificationTestingService(
    private val notificationFactory: NotificationFactory,
) {
    fun test(notificationTesting: NotificationTestingDto): NotificationTestResult {
        val handler =
            notificationFactory.getNotification(notificationTesting.type)
                ?: return NotificationTestResult.Failure("Unknown notification type: ${notificationTesting.type}")

        @Suppress("UNCHECKED_CAST")
        val typedHandler = handler as TypedNotificationInterfaces<Any>

        return try {
            typedHandler.sendTest(notificationTesting.content)
            NotificationTestResult.Success
        } catch (e: Exception) {
            logger.error(e) { "Failed send test notification: ${e.message}" }
            NotificationTestResult.Failure(e.message ?: "Failed send test notification")
        }
    }
}
