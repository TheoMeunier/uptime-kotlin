package tmenier.fr.monitors.notifications

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import tmenier.fr.monitors.enums.NotificationChannelsEnum

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
