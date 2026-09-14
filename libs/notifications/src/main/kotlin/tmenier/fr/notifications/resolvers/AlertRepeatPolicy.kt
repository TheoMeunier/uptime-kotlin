package tmenier.fr.notifications.resolvers

import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import java.time.Instant

object AlertRepeatPolicy {
    fun isDue(
        status: ProbeMonitorLogStatus,
        alertedStatus: ProbeMonitorLogStatus,
        alertRepeatSeconds: Int,
        nextAlertAt: Instant?,
        at: Instant,
    ): Boolean {
        if (alertRepeatSeconds <= 0) return false
        if (status != ProbeMonitorLogStatus.FAILURE) return false
        if (alertedStatus != ProbeMonitorLogStatus.FAILURE) return false

        return nextAlertAt != null && !nextAlertAt.isAfter(at)
    }

    fun deadlineAfter(
        at: Instant,
        alertRepeatSeconds: Int,
    ): Instant? = if (alertRepeatSeconds > 0) at.plusSeconds(alertRepeatSeconds.toLong()) else null
}
