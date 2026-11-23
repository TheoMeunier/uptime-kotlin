package tmenier.fr.notifications.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.mappers.NotificationMapper
import tmenier.fr.databases.repositories.NotificationRepository
import tmenier.fr.notifications.dto.NotificationTestResult
import tmenier.fr.notifications.dto.NotificationTestingDto
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import tmenier.fr.notifications.services.NotificationTestingService
import tmenier.fr.notifications.services.ResolveNotificationContentService
import java.util.UUID

@ApplicationScoped
class TestingNotificationAction(
    private val notificationTestingService: NotificationTestingService,
    private val resolveNotificationContentService: ResolveNotificationContentService,
    private val notificationRepository: NotificationRepository,
) {
    fun execute(
        payload: BaseStoreNotificationRequest,
        notificationId: UUID? = null,
    ): NotificationTestResult {
        val existing =
            notificationId?.let { id ->
                val entity = notificationRepository.findById(id)
                NotificationMapper.toDto(entity, withSecrets = true).takeIf { it.type == payload.notificationType }
            }

        val notification =
            NotificationTestingDto(
                type = payload.notificationType,
                content = resolveNotificationContentService.resolveForTesting(payload, existing),
            )

        return notificationTestingService.test(notification)
    }
}
