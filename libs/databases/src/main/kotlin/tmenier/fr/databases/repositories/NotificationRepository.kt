package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.entities.NotificationsChannelEntity
import java.util.UUID

@ApplicationScoped
class NotificationRepository {

    fun findByIds(ids: List<UUID>): List<NotificationsChannelEntity> = NotificationsChannelEntity.findByIds(ids)

}
