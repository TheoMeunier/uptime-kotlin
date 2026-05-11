package tmenier.fr.notifications

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import tmenier.fr.notifications.enums.NotificationChannelsEnum

@ApplicationScoped
class NotificationFactory(
    private val notificationServices: Instance<TypedNotificationInterfaces<*>>,
) {
    fun getNotification(notificationType: NotificationChannelsEnum): TypedNotificationInterfaces<*>? =
        notificationServices
            .stream()
            .filter { it.getNotificationType() == notificationType.name }
            .findFirst()
            .orElse(null)
}
