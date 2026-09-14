package tmenier.fr.notifications.resolvers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import java.time.Instant

class AlertRepeatPolicyTest {
    private val now: Instant = Instant.parse("2026-09-14T03:00:00Z")

    private fun isDue(
        status: ProbeMonitorLogStatus = ProbeMonitorLogStatus.FAILURE,
        alertedStatus: ProbeMonitorLogStatus = ProbeMonitorLogStatus.FAILURE,
        alertRepeatSeconds: Int = 1800,
        nextAlertAt: Instant? = now.minusSeconds(1),
    ) = AlertRepeatPolicy.isDue(
        status = status,
        alertedStatus = alertedStatus,
        alertRepeatSeconds = alertRepeatSeconds,
        nextAlertAt = nextAlertAt,
        at = now,
    )

    @Test
    fun `an overdue deadline on an announced outage is due`() {
        assertTrue(isDue())
        assertTrue(isDue(nextAlertAt = now), "a deadline reached exactly now must fire, not wait a full period")
    }

    @Test
    fun `resend disabled never fires`() {
        assertFalse(isDue(alertRepeatSeconds = 0))
        assertFalse(isDue(alertRepeatSeconds = -1))
    }

    @Test
    fun `a deadline still ahead waits`() {
        assertFalse(isDue(nextAlertAt = now.plusSeconds(1)))
    }

    @Test
    fun `no armed deadline means no open alert to repeat`() {
        assertFalse(isDue(nextAlertAt = null))
    }

    @Test
    fun `only an outage that was announced gets repeated`() {
        // Recovered, or still retrying: nothing is being repeated.
        assertFalse(isDue(status = ProbeMonitorLogStatus.SUCCESS))
        assertFalse(isDue(status = ProbeMonitorLogStatus.WARNING))
        assertFalse(isDue(status = ProbeMonitorLogStatus.PAUSE))

        // Down, but the channels were never told -- that is the transition's job,
        // and repeating here would alert before the first alert.
        assertFalse(isDue(alertedStatus = ProbeMonitorLogStatus.SUCCESS))
        assertFalse(isDue(alertedStatus = ProbeMonitorLogStatus.WARNING))
    }

    @Test
    fun `the next deadline is one period away, and none when disabled`() {
        assertEquals(now.plusSeconds(1800), AlertRepeatPolicy.deadlineAfter(now, 1800))
        assertNull(AlertRepeatPolicy.deadlineAfter(now, 0))
    }
}
