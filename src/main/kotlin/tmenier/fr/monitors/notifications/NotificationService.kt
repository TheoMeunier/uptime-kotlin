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
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.util.*

@ApplicationScoped
class NotificationService(
    val notificationFactory: NotificationFactory,
) {
    @Transactional
    suspend fun sendNotification(
        probeId: UUID,
        result: ProbeResult,
    ) {
        val probe = ProbesEntity.findById(probeId) ?: throw NotFoundException("Probe not found: $probeId")
        val notifications = probe.notifications

        if (probe.status === result.status) return

        coroutineScope {
            notifications.forEach { notification ->
                launch {
                    val content = NotificationContentMapper.toDTO(notification)
                    val handler = notificationFactory.getNotification(notification.type)

                    if (handler == null) {
                        logger.warn { "Unknown notification handle type: ${notification.type}" }
                        return@launch
                    }

                    @Suppress("UNCHECKED_CAST")
                    val typedHandler = handler as TypedNotificationInterfaces<Any>

                    if (probe.status === ProbeMonitorLogStatus.SUCCESS && result.status === ProbeMonitorLogStatus.FAILURE) {
                        typedHandler.sendFailure(content, probe, result)
                    } else if (probe.status === ProbeMonitorLogStatus.FAILURE && result.status === ProbeMonitorLogStatus.SUCCESS) {
                        typedHandler.sendSuccess(content, probe, result)
                    } else {
                        return@launch
                    }
                }
            }
        }

        logger.info("Probe ${probe.id} sent notifications")
    }
}
