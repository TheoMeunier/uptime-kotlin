package tmenier.fr.monitors.notifications

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import tmenier.fr.monitors.enums.NotificationChannelsEnum

@ApplicationScoped
class NotificationFactory(
    private val notificationServices: Instance<NotificationInterfaces>
) {
    fun getNotification(notificationType: NotificationChannelsEnum): NotificationInterfaces? {
        return notificationServices.stream()
            .filter { it.getNotificationType() == notificationType.name } // == au lieu de ===
            .findFirst()
            .orElse(null)
    }
}