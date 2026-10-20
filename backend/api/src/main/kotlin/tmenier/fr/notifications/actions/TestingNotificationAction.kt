package tmenier.fr.notifications.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.notifications.dto.NotificationTestResult
import tmenier.fr.notifications.dto.NotificationTestingDto
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import tmenier.fr.notifications.services.NotificationTestingService
import tmenier.fr.notifications.services.ResolveNotificationContentService

@ApplicationScoped
class TestingNotificationAction(
    private val notificationTestingService: NotificationTestingService,
    private val resolveNotificationContentService: ResolveNotificationContentService,
) {
    fun execute(payload: BaseStoreNotificationRequest): NotificationTestResult {
        val notification =
            NotificationTestingDto(
                type = payload.notificationType,
                content = resolveNotificationContentService.resolveForTesting(payload),
            )

        return notificationTestingService.test(notification)
    }
}
