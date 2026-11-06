package tmenier.fr.schedulers.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.probes.PushSignal
import java.time.Instant

class PushHeartbeatPolicyTest {
    private val armedAt = Instant.parse("2026-10-01T00:00:00Z")
    private val day = 86_400
    private val hour = 3_600

    private fun evaluate(
        now: Instant,
        lastPushAt: Instant? = null,
        signal: PushSignal? = lastPushAt?.let { PushSignal.UP },
        message: String? = null,
    ) = PushHeartbeatPolicy.evaluate(
        now = now,
        armedAt = armedAt,
        lastPushAt = lastPushAt,
        lastPushSignal = signal,
        lastPushMessage = message,
        periodSeconds = day,
        graceSeconds = hour,
    )

    @Test
    fun `a new monitor waits for its first ping instead of failing`() {
        val verdict = evaluate(now = armedAt.plusSeconds(10L * hour))

        assertEquals(ProbeMonitorLogStatus.SUCCESS, verdict.status)
        assertTrue(verdict.message.startsWith("Waiting for first ping"), verdict.message)
    }

    @Test
    fun `a monitor never pinged fails once period and grace have elapsed since arming`() {
        assertEquals(ProbeMonitorLogStatus.SUCCESS, evaluate(now = armedAt.plusSeconds(day + hour.toLong())).status)

        val verdict = evaluate(now = armedAt.plusSeconds(day + hour + 1L))
        assertEquals(ProbeMonitorLogStatus.FAILURE, verdict.status)
        assertTrue(verdict.message.startsWith("No ping received since monitoring started"), verdict.message)
    }

    @Test
    fun `the deadline runs from the last ping`() {
        val lastPush = armedAt.plusSeconds(5L * day)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, evaluate(lastPush.plusSeconds(day + 30L * 60), lastPush).status)

        val late = evaluate(lastPush.plusSeconds(day + hour + 60L), lastPush)
        assertEquals(ProbeMonitorLogStatus.FAILURE, late.status)
        assertEquals("No ping received for 1d 1h 1m (expected every 1d)", late.message)
    }

    @Test
    fun `an explicit failure fails immediately and keeps the job message`() {
        val lastPush = armedAt.plusSeconds(hour.toLong())

        val verdict = evaluate(lastPush.plusSeconds(120), lastPush, PushSignal.DOWN, "disk full")

        assertEquals(ProbeMonitorLogStatus.FAILURE, verdict.status)
        assertEquals("Job reported a failure 2m ago: disk full", verdict.message)
    }

    @Test
    fun `a ping received before re-arming no longer counts`() {
        val stalePing = armedAt.minusSeconds(30L * day)

        val waiting = evaluate(armedAt.plusSeconds(hour.toLong()), stalePing, PushSignal.DOWN)
        assertEquals(ProbeMonitorLogStatus.SUCCESS, waiting.status)
        assertTrue(waiting.message.startsWith("Waiting for first ping"), waiting.message)
    }

    @Test
    fun `a recovery ping after an explicit failure brings the monitor back up`() {
        val failure = armedAt.plusSeconds(hour.toLong())
        val recovery = failure.plusSeconds(600)

        val verdict = evaluate(recovery.plusSeconds(30), recovery, PushSignal.UP)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, verdict.status)
    }

    @Test
    fun `a healthy ping reports how long ago it arrived`() {
        val lastPush = armedAt.plusSeconds(hour.toLong())

        val verdict = evaluate(lastPush.plusSeconds(5L * 60), lastPush, message = "42 files")

        assertEquals(ProbeMonitorLogStatus.SUCCESS, verdict.status)
        assertEquals("Last ping 5m ago: 42 files", verdict.message)
    }
}
