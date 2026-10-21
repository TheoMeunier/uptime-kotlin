package tmenier.fr.schedulers.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.quarkus.runtime.StartupEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class BuildInfoMetricsTest {
    private val registry = SimpleMeterRegistry()

    @Test
    fun `publishes the version and the start time`() {
        val before = Instant.now().epochSecond
        BuildInfoMetrics(registry).apply { version = "1.5.0" }.register(StartupEvent())

        assertEquals(
            1.0,
            registry
                .get(BuildInfoMetrics.BUILD_INFO)
                .tag("version", "1.5.0")
                .gauge()
                .value(),
        )
        val startedAt = registry.get(BuildInfoMetrics.START_TIME).gauge().value()
        assertTrue(startedAt >= before && startedAt <= Instant.now().epochSecond, "start time $startedAt")
    }
}
