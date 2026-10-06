package tmenier.fr.notifications.jobs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

class DeliveryWatchdogTest {
    private val t0 = Instant.parse("2026-10-06T10:00:00Z")

    private class MutableClock(
        var now: Instant,
    ) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC

        override fun withZone(zone: ZoneId?): Clock = this

        override fun instant(): Instant = now
    }

    @Test
    fun `a delivery within budget keeps its lease and is not interrupted`() {
        val clock = MutableClock(t0)
        val watchdog = DeliveryWatchdog(Duration.ofSeconds(60), clock)
        val id = UUID.randomUUID()

        watchdog.started(id)
        clock.now = t0.plusSeconds(59)

        assertEquals(setOf(id), watchdog.renewable())
        assertTrue(watchdog.interruptOverdue().isEmpty())
        assertFalse(Thread.interrupted())
    }

    @Test
    fun `an overdue delivery is interrupted once and loses its lease`() {
        val clock = MutableClock(t0)
        val watchdog = DeliveryWatchdog(Duration.ofSeconds(60), clock)
        val id = UUID.randomUUID()

        watchdog.started(id)
        clock.now = t0.plusSeconds(61)

        assertEquals(listOf(id), watchdog.interruptOverdue())
        assertTrue(Thread.interrupted(), "the worker thread must be interrupted")
        assertTrue(watchdog.renewable().isEmpty())

        // A second pass does not interrupt the same delivery again.
        assertTrue(watchdog.interruptOverdue().isEmpty())
        assertFalse(Thread.interrupted())
    }

    @Test
    fun `a finished delivery is neither renewed nor interrupted`() {
        val clock = MutableClock(t0)
        val watchdog = DeliveryWatchdog(Duration.ofSeconds(60), clock)
        val id = UUID.randomUUID()

        watchdog.started(id)
        watchdog.finished(id)
        clock.now = t0.plusSeconds(120)

        assertTrue(watchdog.renewable().isEmpty())
        assertTrue(watchdog.interruptOverdue().isEmpty())
        assertFalse(Thread.interrupted())
    }

    @Test
    fun `interrupts a worker blocked on a call that never returns`() {
        val clock = MutableClock(t0)
        val watchdog = DeliveryWatchdog(Duration.ofSeconds(60), clock)
        val id = UUID.randomUUID()
        val started = java.util.concurrent.CountDownLatch(1)
        var outcome: Throwable? = null

        val worker =
            Thread {
                watchdog.started(id)
                started.countDown()
                try {
                    Thread.sleep(Long.MAX_VALUE)
                } catch (e: InterruptedException) {
                    outcome = e
                } finally {
                    watchdog.finished(id)
                }
            }
        worker.start()
        started.await()

        clock.now = t0.plusSeconds(61)
        assertEquals(listOf(id), watchdog.interruptOverdue())
        worker.join(5_000)

        assertFalse(worker.isAlive)
        assertTrue(outcome is InterruptedException)
    }
}
