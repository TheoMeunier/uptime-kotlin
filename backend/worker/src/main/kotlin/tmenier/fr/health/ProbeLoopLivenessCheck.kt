package tmenier.fr.health

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Liveness
import tmenier.fr.monitors.ProbeLoopHeartbeat
import java.time.Duration
import java.time.Instant

@Liveness
@ApplicationScoped
class ProbeLoopLivenessCheck(
    private val probeLoopHeartbeat: ProbeLoopHeartbeat,
) : HealthCheck {
    @ConfigProperty(name = "worker.health.max-tick-lag", defaultValue = "PT1M")
    lateinit var maxTickLag: Duration

    override fun call(): HealthCheckResponse {
        val lag = Duration.between(probeLoopHeartbeat.lastActivityAt(), Instant.now())

        val response =
            HealthCheckResponse
                .named("probe-worker-loop")
                .withData("last_tick_at", probeLoopHeartbeat.lastTickAt()?.toString() ?: "never")
                .withData("lag_seconds", lag.seconds)
                .withData("max_lag_seconds", maxTickLag.seconds)

        return if (lag <= maxTickLag) response.up().build() else response.down().build()
    }
}
