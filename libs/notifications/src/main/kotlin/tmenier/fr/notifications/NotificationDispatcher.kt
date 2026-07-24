package tmenier.fr.notifications

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.notifications.NotificationEvent
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.NotificationDto
import tmenier.fr.databases.dtos.NotificationQueueRetryDto
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.repositories.NotificationTaskRepository
import java.util.UUID

@ApplicationScoped
class NotificationDispatcher(
    private val notificationFactory: NotificationFactory,
    private val notificationTaskRepository: NotificationTaskRepository
) {

    fun dispatch(
        notification: NotificationDto,
        probe: ProbeDTO,
        result: ProbeResult,
        event: NotificationEvent,
        taskId: UUID?
    ) {
        if (event == NotificationEvent.NONE) return

        val handler = notificationFactory.getNotification(notification.type)
        if (handler == null) {
            logger.warn { "Unknown notification handler type: ${notification.type}" }
            return
        }

        @Suppress("UNCHECKED_CAST")
        val typedHandler = handler as TypedNotificationInterfaces<Any>

        try {

            when (event) {
                NotificationEvent.FAILURE -> typedHandler.sendFailure(notification.content, probe, result)
                NotificationEvent.RECOVERY -> typedHandler.sendSuccess(notification.content, probe, result)
            }

        } catch (e: Exception) {
            logger.error(e) { "Failed to send notification for probe ${probe.id}" }

            notificationTaskRepository.enqueueForRetry(
                NotificationQueueRetryDto(
                    probeId = probe.id,
                    taskId = taskId,
                    notificationId = notification.id,
                    errorMessage = e.message ?: "Unknown error",
                    channel = notification.type,
                    payload = result,
                    event = event
                )
            )
        }
    }

}
