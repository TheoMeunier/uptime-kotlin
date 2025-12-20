package tmenier.fr.monitors.notifications

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.coroutineScope
import tmenier.fr.common.utils.logger
import tmenier.fr.monitors.dtos.responses.ProbeMonitorDTO
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.entities.mapper.NotificationContentMapper
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus

@ApplicationScoped
class NotificationService(
    val notificationFactory: NotificationFactory
) {

    suspend fun sendNotification(probe: ProbesEntity, result: ProbeMonitorDTO) {
        val notifications = probe.notifications

        coroutineScope {
            notifications.forEach { notification ->
                val content = NotificationContentMapper.toDTO(notification)
                val handler = notificationFactory.getNotification(notification.type)

                if (handler == null) {
                    logger.warn("No handler found for notification type: ${notification.type}")
                    return@forEach
                }

                when (result.status) {
                    ProbeMonitorLogStatus.SUCCESS -> handler.sendSuccess(content, probe, result)
                    ProbeMonitorLogStatus.FAILURE -> handler.sendFailure(content, probe, result)
                    else -> logger.warn("Unknown status: ${result.status}")
                }
            }
        }

        logger.info("Probe ${probe.id} sent notifications")
    }
}