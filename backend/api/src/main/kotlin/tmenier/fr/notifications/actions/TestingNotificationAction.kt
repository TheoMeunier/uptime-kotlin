package tmenier.fr.notifications.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.notifications.NotificationService
import tmenier.fr.notifications.dto.NotificationTestingDto
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import tmenier.fr.notifications.services.ResolveNotificationContentService

@ApplicationScoped
class TestingNotificationAction(
    private val notificationService: NotificationService,
    private val resolveNotificationContentService: ResolveNotificationContentService,
) {
    fun execute(payload: BaseStoreNotificationRequest) {
        val notification =
            NotificationTestingDto(
                type = payload.notificationType,
                content = resolveNotificationContentService.resolveForTesting(payload),
            )

        notificationService.sendTest(notification)
    }
}
