package tmenier.fr.notifications.resolvers

import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.utils.toHumanReadable
import java.time.Duration
import java.time.Instant

object OutageWindow {
    fun startAfter(
        current: Instant?,
        status: ProbeMonitorLogStatus,
        at: Instant,
    ): Instant? =
        when (status) {
            ProbeMonitorLogStatus.SUCCESS -> null
            ProbeMonitorLogStatus.WARNING, ProbeMonitorLogStatus.FAILURE -> current ?: at
            ProbeMonitorLogStatus.PAUSE -> current
        }

    fun downtime(
        start: Instant?,
        recoveredAt: Instant,
    ): Duration? =
        start
            ?.let { Duration.between(it, recoveredAt) }
            ?.takeUnless { it.isNegative }

    fun suffix(downtime: Duration?): String = downtime?.let { " (down for ${it.toHumanReadable()})" } ?: ""
}
