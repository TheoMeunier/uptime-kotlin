package tmenier.fr.notifications.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationEvent
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.repositories.NotificationTaskRepository
import tmenier.fr.notifications.resolvers.AlertRepeatPolicy
import tmenier.fr.notifications.resolvers.NotificationEventResolver
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class NotificationService(
    private val notificationEventResolver: NotificationEventResolver,
    private val notificationTaskRepository: NotificationTaskRepository,
) {
    fun announce(
        probe: ProbesEntity,
        checkTaskId: UUID,
        result: ProbeResult,
        at: Instant,
    ): NotificationEvent {
        val announcedStatus = probe.alertedStatus
        val event = notificationEventResolver.resolve(announcedStatus, result.status)

        if (event != NotificationEvent.NONE) {
            notificationTaskRepository.enqueueDeliveries(probe, checkTaskId, result, event)
            applyAnnouncedStatus(probe, result.status, at)

            logger.info {
                "Resolved notification transition for Probe Check $checkTaskId: " +
                    "$announcedStatus -> ${result.status}, event=$event, channels=${probe.notifications.size}, " +
                    "nextAlertAt=${probe.nextAlertAt}"
            }
            return event
        }

        val due =
            AlertRepeatPolicy.isDue(
                status = result.status,
                alertedStatus = announcedStatus,
                alertRepeatSeconds = probe.alertRepeatSeconds,
                nextAlertAt = probe.nextAlertAt,
                at = at,
            )
        if (!due) return NotificationEvent.NONE

        val reminderIndex = probe.alertRepeatCount + 1
        notificationTaskRepository.enqueueDeliveries(
            probe = probe,
            checkTaskId = checkTaskId,
            result = result,
            event = NotificationEvent.REMINDER,
            reminderIndex = reminderIndex,
        )
        probe.alertRepeatCount = reminderIndex
        probe.nextAlertAt = AlertRepeatPolicy.deadlineAfter(at, probe.alertRepeatSeconds)

        logger.info {
            "Resent alert for Probe Check $checkTaskId: probe=${probe.id} still FAILURE, " +
                "reminder=#$reminderIndex, channels=${probe.notifications.size}, nextAlertAt=${probe.nextAlertAt}"
        }
        return NotificationEvent.REMINDER
    }

    private fun applyAnnouncedStatus(
        probe: ProbesEntity,
        announced: ProbeMonitorLogStatus,
        at: Instant,
    ) {
        probe.alertedStatus = announced
        probe.alertRepeatCount = 0
        probe.nextAlertAt =
            if (announced == ProbeMonitorLogStatus.FAILURE) {
                AlertRepeatPolicy.deadlineAfter(at, probe.alertRepeatSeconds)
            } else {
                null
            }
    }
}
