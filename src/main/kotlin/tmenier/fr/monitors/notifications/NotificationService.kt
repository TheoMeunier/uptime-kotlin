package tmenier.fr.monitors.notifications

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.common.utils.logger
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.entities.mapper.NotificationContentMapper
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.notifications.dto.NotificationTestingDto
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.util.UUID

@ApplicationScoped
class NotificationService(
    val notificationFactory: NotificationFactory,
) {
    @Transactional
    suspend fun sendNotification(
        probeId: UUID,
        result: ProbeResult,
        previousStatus: ProbeMonitorLogStatus,
    ) {
        val probe = ProbesEntity.findById(probeId) ?: throw NotFoundException("Probe not found: $probeId")
        val notifications = probe.notifications

        if (previousStatus == result.status) {
            logger.debug { "Probe ${probe.id}: No status change ($previousStatus), skipping notifications" }
            return
        }

        logger.info { "Probe ${probe.id}: Status changed from $previousStatus to ${result.status}" }

        coroutineScope {
            notifications.forEach { notification ->
                launch {
                    val content = NotificationContentMapper.toDTO(notification)
                    val handler = notificationFactory.getNotification(notification.type)

                    if (handler == null) {
                        logger.warn { "Unknown notification handler type: ${notification.type}" }
                        return@launch
                    }

                    @Suppress("UNCHECKED_CAST")
                    val typedHandler = handler as TypedNotificationInterfaces<Any>

                    try {
                        when {
                            previousStatus != ProbeMonitorLogStatus.FAILURE && result.status == ProbeMonitorLogStatus.FAILURE -> {
                                logger.info { "Sending failure notification for probe ${probe.id}" }
                                typedHandler.sendFailure(content, probe, result)
                            }

                            previousStatus == ProbeMonitorLogStatus.FAILURE && result.status == ProbeMonitorLogStatus.SUCCESS -> {
                                logger.info { "Sending recovery notification for probe ${probe.id}" }
                                typedHandler.sendSuccess(content, probe, result)
                            }

                            else -> {
                                logger.debug {
                                    "Probe ${probe.id}: Status change $previousStatus -> ${result.status} doesn't trigger notifications"
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to send notification for probe ${probe.id}" }
                    }
                }
            }
        }

        logger.info { "Probe ${probe.id}: Notifications sent successfully" }
    }

    fun sendTest(notificationTesting: NotificationTestingDto) {
        val handler = notificationFactory.getNotification(notificationTesting.type)

        if (handler == null) {
            logger.warn { "Unknown notification handler type: ${notificationTesting.type}" }
            return
        }

        @Suppress("UNCHECKED_CAST")
        val typedHandler = handler as TypedNotificationInterfaces<Any>

        try {
            typedHandler.sendTest(notificationTesting.content)
        } catch (e: Exception) {
            logger.error(e) { "Failed send test notification" }
        }
    }
}
