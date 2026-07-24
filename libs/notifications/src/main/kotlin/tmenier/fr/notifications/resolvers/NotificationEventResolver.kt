package tmenier.fr.notifications.resolvers

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationEvent

@ApplicationScoped
class NotificationEventResolver {

    fun resolve(
        previousStatus: ProbeMonitorLogStatus,
        newStatus: ProbeMonitorLogStatus,
    ): NotificationEvent = when {
        previousStatus != ProbeMonitorLogStatus.FAILURE && newStatus == ProbeMonitorLogStatus.FAILURE -> NotificationEvent.FAILURE
        previousStatus == ProbeMonitorLogStatus.FAILURE && newStatus == ProbeMonitorLogStatus.SUCCESS -> NotificationEvent.RECOVERY
        else -> NotificationEvent.NONE
    }
}
