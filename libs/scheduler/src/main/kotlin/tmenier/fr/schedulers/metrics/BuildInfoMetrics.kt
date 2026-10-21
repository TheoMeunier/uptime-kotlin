package tmenier.fr.schedulers.metrics

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Supplier

@ApplicationScoped
class BuildInfoMetrics(
    private val registry: MeterRegistry,
) {
    @ConfigProperty(name = "uptime.version", defaultValue = "dev")
    lateinit var version: String

    private val startedAt = AtomicLong()

    fun register(
        @Observes event: StartupEvent,
    ) {
        startedAt.set(Instant.now().epochSecond)
        Gauge
            .builder(BUILD_INFO, Supplier<Number> { 1 })
            .description("Always 1; the version label tells which version this process runs")
            .tag("version", version)
            .register(registry)
        Gauge
            .builder(START_TIME, startedAt) { value -> value.get().toDouble() }
            .description("Start time of the process, in seconds since the epoch")
            .register(registry)
    }

    companion object {
        const val BUILD_INFO = "uptime.build.info"
        const val START_TIME = "uptime.process.start.time.seconds"
    }
}
