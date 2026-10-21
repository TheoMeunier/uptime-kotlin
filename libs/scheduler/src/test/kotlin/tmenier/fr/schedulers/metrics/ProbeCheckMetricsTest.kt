package tmenier.fr.schedulers.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ProbeCheckMetricsTest {
    private val registry = SimpleMeterRegistry()
    private val metrics = ProbeCheckMetrics(registry)

    @Test
    fun `counts each check under its protocol and result`() {
        metrics.recordCheck(ProbeProtocol.HTTP, ProbeMonitorLogStatus.SUCCESS, Duration.ofMillis(120))
        metrics.recordCheck(ProbeProtocol.HTTP, ProbeMonitorLogStatus.SUCCESS, Duration.ofMillis(80))
        metrics.recordCheck(ProbeProtocol.HTTP, ProbeMonitorLogStatus.FAILURE, Duration.ofSeconds(10))

        assertEquals(2.0, checks("http", "success"))
        assertEquals(1.0, checks("http", "failure"))
    }

    @Test
    fun `a check that threw is counted as an error`() {
        metrics.recordCheck(ProbeProtocol.DNS, null, Duration.ofMillis(5))

        assertEquals(1.0, checks("dns", ProbeCheckMetrics.RESULT_ERROR))
    }

    @Test
    fun `times every check of a protocol whatever its result`() {
        metrics.recordCheck(ProbeProtocol.TCP, ProbeMonitorLogStatus.SUCCESS, Duration.ofMillis(100))
        metrics.recordCheck(ProbeProtocol.TCP, null, Duration.ofMillis(300))

        val timer = registry.find(ProbeCheckMetrics.CHECK_DURATION).tag("protocol", "tcp").timer()
        assertNotNull(timer)
        assertEquals(2L, timer!!.count())
        assertEquals(400.0, timer.totalTime(TimeUnit.MILLISECONDS), 0.001)
    }

    @Test
    fun `the worker gauges follow the active task counter`() {
        val active = AtomicInteger()
        metrics.bindWorker("eu-west", concurrency = 4, activeTasks = active)
        active.set(3)

        assertEquals(
            3.0,
            registry
                .get(ProbeCheckMetrics.WORKER_ACTIVE)
                .tag("region", "eu-west")
                .gauge()
                .value(),
        )
        assertEquals(
            4.0,
            registry
                .get(ProbeCheckMetrics.WORKER_CONCURRENCY)
                .tag("region", "eu-west")
                .gauge()
                .value(),
        )
    }

    private fun checks(
        protocol: String,
        result: String,
    ): Double =
        registry
            .find(ProbeCheckMetrics.CHECKS)
            .tags("protocol", protocol, "result", result)
            .counter()
            ?.count() ?: 0.0
}
