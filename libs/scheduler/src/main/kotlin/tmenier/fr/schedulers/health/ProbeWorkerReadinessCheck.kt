package tmenier.fr.schedulers.health

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Readiness
import tmenier.fr.common.config.SchedulerStrategy
import tmenier.fr.schedulers.queue.ProbeLoopHeartbeat

@Readiness
@ApplicationScoped
class ProbeWorkerReadinessCheck(
    private val probeLoopHeartbeat: ProbeLoopHeartbeat,
    private val schedulerStrategy: SchedulerStrategy,
) : HealthCheck {
    @ConfigProperty(name = "scheduler.worker.name", defaultValue = "default")
    lateinit var region: String

    override fun call(): HealthCheckResponse =
        HealthCheckResponse
            .named("probe-worker-scheduler")
            .status(probeLoopHeartbeat.hasTicked())
            .withData("strategy", schedulerStrategy.value)
            .withData("region", region)
            .build()
}
