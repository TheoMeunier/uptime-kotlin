package tmenier.fr.health

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Readiness
import tmenier.fr.monitors.ProbeLoopHeartbeat

@Readiness
@ApplicationScoped
class ProbeWorkerReadinessCheck(
    private val probeLoopHeartbeat: ProbeLoopHeartbeat,
) : HealthCheck {
    @ConfigProperty(name = "scheduler.strategy", defaultValue = "none")
    lateinit var strategy: String

    @ConfigProperty(name = "scheduler.worker.name", defaultValue = "default")
    lateinit var region: String

    override fun call(): HealthCheckResponse =
        HealthCheckResponse
            .named("probe-worker-scheduler")
            .status(probeLoopHeartbeat.hasTicked())
            .withData("strategy", strategy)
            .withData("region", region)
            .build()
}
