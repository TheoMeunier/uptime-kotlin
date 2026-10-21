package tmenier.fr.schedulers.metrics

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.OperationalMetricsRepository
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@ApplicationScoped
class WorkerMetrics(
    private val registry: MeterRegistry,
    private val repository: OperationalMetricsRepository,
) {
    private val workers = ConcurrentHashMap<String, RegisteredWorker>()

    @Scheduled(every = "15s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun refresh() {
        try {
            val now = Instant.now()
            val seen =
                repository
                    .workerHeartbeats()
                    .map { heartbeat ->
                        val key = "${heartbeat.workerId}|${heartbeat.region}"
                        val age = Duration.between(heartbeat.lastSeenAt, now).seconds.coerceAtLeast(0)
                        workers.computeIfAbsent(key) { _ -> register(heartbeat.workerId, heartbeat.region) }.age.set(age)
                        key
                    }.toSet()

            (workers.keys - seen).forEach { key ->
                workers.remove(key)?.let { worker -> registry.remove(worker.gauge) }
            }
        } catch (error: Exception) {
            logger.warn(error) { "Failed to refresh the worker metrics" }
        }
    }

    private fun register(
        workerId: String,
        region: String,
    ): RegisteredWorker {
        val age = AtomicLong()
        val gauge =
            Gauge
                .builder(HEARTBEAT_AGE, age) { value -> value.get().toDouble() }
                .description("Seconds since the last heartbeat of an instance running Probe Checks")
                .tag("worker", workerId)
                .tag("region", region)
                .register(registry)
        return RegisteredWorker(gauge, age)
    }

    private class RegisteredWorker(
        val gauge: Gauge,
        val age: AtomicLong,
    )

    companion object {
        const val HEARTBEAT_AGE = "uptime.worker.heartbeat.age.seconds"
    }
}
