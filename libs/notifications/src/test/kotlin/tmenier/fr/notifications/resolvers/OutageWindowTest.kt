package tmenier.fr.notifications.resolvers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import java.time.Duration
import java.time.Instant

class OutageWindowTest {
    private val t0 = Instant.parse("2026-09-18T10:00:00Z")
    private val later = t0.plusSeconds(750)

    @Test
    fun `outage starts at the first failed attempt, retries included`() {
        assertEquals(t0, OutageWindow.startAfter(null, ProbeMonitorLogStatus.WARNING, t0))
        assertEquals(t0, OutageWindow.startAfter(null, ProbeMonitorLogStatus.FAILURE, t0))
    }

    @Test
    fun `further failures keep the original start`() {
        assertEquals(t0, OutageWindow.startAfter(t0, ProbeMonitorLogStatus.WARNING, later))
        assertEquals(t0, OutageWindow.startAfter(t0, ProbeMonitorLogStatus.FAILURE, later))
    }

    @Test
    fun `a success closes the outage and a pause leaves it untouched`() {
        assertNull(OutageWindow.startAfter(t0, ProbeMonitorLogStatus.SUCCESS, later))
        assertEquals(t0, OutageWindow.startAfter(t0, ProbeMonitorLogStatus.PAUSE, later))
        assertNull(OutageWindow.startAfter(null, ProbeMonitorLogStatus.PAUSE, later))
    }

    @Test
    fun `downtime is measured from the outage start to the recovery`() {
        assertEquals(Duration.ofSeconds(750), OutageWindow.downtime(t0, later))
    }

    @Test
    fun `unknown or inconsistent start gives no downtime`() {
        assertNull(OutageWindow.downtime(null, later))
        assertNull(OutageWindow.downtime(later, t0))
    }

    @Test
    fun `suffix is human readable and empty when unknown`() {
        assertEquals(" (down for 12m 30s)", OutageWindow.suffix(Duration.ofSeconds(750)))
        assertEquals("", OutageWindow.suffix(null))
    }
}
