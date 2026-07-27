package tmenier.fr.notifications.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationEvent
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.repositories.NotificationTaskRepository
import tmenier.fr.notifications.resolvers.NotificationEventResolver
import java.util.UUID

@ApplicationScoped
class NotificationService(
    private val notificationEventResolver: NotificationEventResolver,
    private val notificationTaskRepository: NotificationTaskRepository,
) {
    fun enqueueForTransition(
        probe: ProbesEntity,
        checkTaskId: UUID,
        result: ProbeResult,
        previousStatus: ProbeMonitorLogStatus,
    ): NotificationEvent {
        val event = notificationEventResolver.resolve(previousStatus, result.status)
        notificationTaskRepository.enqueueDeliveries(probe, checkTaskId, result, event)
        logger.info {
            "Resolved notification transition for Probe Check $checkTaskId: " +
                "$previousStatus -> ${result.status}, event=$event, channels=${probe.notifications.size}"
        }
        return event
    }
}
