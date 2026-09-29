package tmenier.fr.schedulers.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class ProbeTimeoutTest {
    @Test
    fun `keeps a timeout within bounds`() {
        assertEquals(30, ProbeTimeout.seconds(30))
        assertEquals(Duration.ofSeconds(30), ProbeTimeout.duration(30))
    }

    @Test
    fun `falls back to the default for zero or negative values`() {
        assertEquals(ProbeTimeout.DEFAULT_SECONDS, ProbeTimeout.seconds(0))
        assertEquals(ProbeTimeout.DEFAULT_SECONDS, ProbeTimeout.seconds(-3))
    }

    @Test
    fun `clamps values above the maximum`() {
        assertEquals(ProbeTimeout.MAX_SECONDS, ProbeTimeout.seconds(1_000))
    }

    @Test
    fun `converts to milliseconds for socket based probes`() {
        assertEquals(12_000L, ProbeTimeout.duration(12).toMillis())
    }
}
