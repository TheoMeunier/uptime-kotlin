package tmenier.fr.schedulers.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

@ApplicationScoped
class ProbeCheckMetrics(
    private val registry: MeterRegistry,
) {
    fun recordCheck(
        protocol: ProbeProtocol,
        status: ProbeMonitorLogStatus?,
        duration: Duration,
    ) {
        val protocolTag = protocol.name.lowercase()
        Counter
            .builder(CHECKS)
            .description("Probe Checks run by this instance, by protocol and result")
            .tag("protocol", protocolTag)
            .tag("result", status?.name?.lowercase() ?: RESULT_ERROR)
            .register(registry)
            .increment()
        Timer
            .builder(CHECK_DURATION)
            .description("Wall-clock duration of a Probe Check, timeout included")
            .tag("protocol", protocolTag)
            .serviceLevelObjectives(*DURATION_BUCKETS)
            .register(registry)
            .record(duration)
    }

    fun bindWorker(
        region: String,
        concurrency: Int,
        activeTasks: AtomicInteger,
    ) {
        Gauge
            .builder(WORKER_ACTIVE, activeTasks) { it.get().toDouble() }
            .description("Probe Check tasks currently running in this worker")
            .tag("region", region)
            .register(registry)
        Gauge
            .builder(WORKER_CONCURRENCY, activeTasks) { concurrency.toDouble() }
            .description("Probe Check tasks this worker may run at once (scheduler.worker.concurrency)")
            .tag("region", region)
            .register(registry)
    }

    companion object {
        const val CHECKS = "uptime.probe.checks"
        const val CHECK_DURATION = "uptime.probe.check.duration"
        const val WORKER_ACTIVE = "uptime.probe.worker.active"
        const val WORKER_CONCURRENCY = "uptime.probe.worker.concurrency"
        const val RESULT_ERROR = "error"

        private val DURATION_BUCKETS: Array<Duration> =
            arrayOf(
                Duration.ofMillis(50),
                Duration.ofMillis(100),
                Duration.ofMillis(250),
                Duration.ofMillis(500),
                Duration.ofSeconds(1),
                Duration.ofMillis(2_500),
                Duration.ofSeconds(5),
                Duration.ofSeconds(10),
                Duration.ofSeconds(30),
            )
    }
}
