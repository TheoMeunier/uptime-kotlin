package tmenier.fr.notifications.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.databases.dtos.NotificationDto
import tmenier.fr.databases.mappers.NotificationMapper
import tmenier.fr.databases.repositories.NotificationRepository
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import tmenier.fr.notifications.services.ResolveNotificationContentService
import java.util.UUID

@ApplicationScoped
class StoreNotificationAction(
    private val notificationChanelRepository: NotificationRepository,
    private val getNotificationContentService: ResolveNotificationContentService
) {

    @Transactional
    fun execute(
        payload: BaseStoreNotificationRequest,
        notificationId: UUID? = null,
    ) {
        val isUpdate = notificationId != null
        val existingNotification = if (isUpdate) {
            NotificationMapper.toDto(notificationChanelRepository.findById(notificationId))
        } else {
            null
        }

        val dto = NotificationDto(
            id = notificationId ?: UUID.randomUUID(),
            name = payload.name,
            type = payload.notificationType,
            isDefault = payload.isDefault ?: false,
            content = getNotificationContentService.resolve(payload, isUpdate, existingNotification),
        )

        if (isUpdate) {
            notificationChanelRepository.update(dto)
        } else {
            notificationChanelRepository.save(dto)
        }
    }
}
