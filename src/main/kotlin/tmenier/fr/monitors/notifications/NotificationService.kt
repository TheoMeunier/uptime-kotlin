package tmenier.fr.monitors.notifications

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.coroutineScope
import tmenier.fr.common.utils.logger
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.entities.mapper.NotificationContentMapper
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.schedulers.dto.ProbeResult

@ApplicationScoped
class NotificationService(
    val notificationFactory: NotificationFactory
) {

    suspend fun sendNotification(probe: ProbesEntity, result: ProbeResult) {
        val notifications = probe.notifications

        if (probe.status === result.status) return

        coroutineScope {
            notifications.forEach { notification ->
                val content = NotificationContentMapper.toDTO(notification)
                val handler = notificationFactory.getNotification(notification.type)

                if (handler == null) {
                    logger.warn("No handler found for notification type: ${notification.type}")
                    return@forEach
                }

                if (probe.status === ProbeMonitorLogStatus.SUCCESS && result.status === ProbeMonitorLogStatus.FAILURE) {
                    handler.sendFailure(content, probe, result)
                } else if (probe.status === ProbeMonitorLogStatus.FAILURE && result.status === ProbeMonitorLogStatus.SUCCESS) {
                    handler.sendSuccess(content, probe, result)
                }
            }
        }

        logger.info("Probe ${probe.id} sent notifications")
    }
}