package tmenier.fr.notifications

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeRepository
import java.util.UUID

@ApplicationScoped
class NotificationService(
    val notificationFactory: NotificationFactory,
    val probeRepository: ProbeRepository
) {
    @Transactional
    suspend fun sendNotification(
        probeId: UUID,
        result: ProbeResult,
        previousStatus: ProbeMonitorLogStatus,
    ) {
        val dto = ProbeMapper.toProbeWithNotificationsDto(withContext(Dispatchers.IO) {
            probeRepository.findById(probeId)
        })
        val notifications = dto.notifications

        if (previousStatus == result.status) {
            logger.debug { "Probe ${dto.probe.id}: No status change ($previousStatus), skipping notifications" }
            return
        }

        logger.info { "Probe ${dto.probe.id}: Status changed from $previousStatus to ${result.status}" }

        coroutineScope {
            notifications.forEach { notification ->
                launch {
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
                                logger.info { "Sending failure notification for probe ${dto.probe.id}" }
                                typedHandler.sendFailure(notification.content, dto.probe, result)
                            }

                            previousStatus == ProbeMonitorLogStatus.FAILURE && result.status == ProbeMonitorLogStatus.SUCCESS -> {
                                logger.info { "Sending recovery notification for probe ${dto.probe.id}" }
                                typedHandler.sendSuccess(notification.content, dto.probe, result)
                            }

                            else -> {
                                logger.debug {
                                    "Probe ${dto.probe.id}: Status change $previousStatus -> ${result.status} doesn't trigger notifications"
                                }
                            }
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to send notification for probe ${dto.probe.id}" }
                    }
                }
            }
        }

        logger.info { "Probe ${dto.probe.id}: Notifications sent successfully" }
    }

    fun sendTest(notificationTesting: tmenier.fr.notifications.dto.NotificationTestingDto) {
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
            logger.error { "Failed send test notification ${e.message}" }
        }
    }
}
