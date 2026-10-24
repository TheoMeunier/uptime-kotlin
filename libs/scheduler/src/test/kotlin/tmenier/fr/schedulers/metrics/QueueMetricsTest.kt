package tmenier.fr.schedulers.metrics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QueueMetricsTest {
    @Test
    fun `expects one check per interval and per probe`() {
        assertEquals(3.2, QueueMetrics.expectedPerMinute(listOf(60, 30, 300)), 1e-9)
    }

    @Test
    fun `no enabled probe, no check expected`() {
        assertEquals(0.0, QueueMetrics.expectedPerMinute(emptyList()))
    }

    @Test
    fun `an interval below one second counts as one second, like the scheduler`() {
        assertEquals(60.0, QueueMetrics.expectedPerMinute(listOf(0)))
    }
}
