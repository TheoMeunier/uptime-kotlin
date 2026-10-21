package tmenier.fr.schedulers.metrics

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.quarkus.runtime.StartupEvent
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.probes.QueueJobStatus
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.OperationalMetricsRepository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@ApplicationScoped
class QueueMetrics(
    private val registry: MeterRegistry,
    private val repository: OperationalMetricsRepository,
) {
    private val values = ConcurrentHashMap<String, AtomicLong>()

    fun register(
        @Observes event: StartupEvent,
    ) {
        QUEUES.forEach { queue ->
            QueueJobStatus.entries.forEach { status -> queueGauge(queue, status) }
            lagGauge(queue)
        }
        PROBE_STATES.forEach { state -> probeGauge(state) }
    }

    @Scheduled(every = "30s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun refresh() {
        try {
            publishQueue(QUEUE_PROBE_CHECKS, repository.probeCheckJobsByStatus())
            publishQueue(QUEUE_NOTIFICATIONS, repository.notificationDeliveriesByStatus())
            lagGauge(QUEUE_PROBE_CHECKS).set(repository.probeCheckJobsLag().seconds)
            lagGauge(QUEUE_NOTIFICATIONS).set(repository.notificationDeliveriesLag().seconds)

            val probes = repository.probesByState().groupBy({ probeState(it.enabled, it.status) }, { it.count })
            PROBE_STATES.forEach { state -> probeGauge(state).set(probes[state]?.sum() ?: 0L) }
        } catch (error: Exception) {
            logger.warn(error) { "Failed to refresh the queue and probe metrics" }
        }
    }

    private fun publishQueue(
        queue: String,
        counts: Map<QueueJobStatus, Long>,
    ) {
        QueueJobStatus.entries.forEach { status -> queueGauge(queue, status).set(counts[status] ?: 0L) }
    }

    private fun queueGauge(
        queue: String,
        status: QueueJobStatus,
    ): AtomicLong =
        gauge(
            QUEUE_TASKS,
            Tags.of("queue", queue, "status", status.name.lowercase()),
            "Tasks in a queue, by status",
        )

    private fun lagGauge(queue: String): AtomicLong =
        gauge(QUEUE_LAG, Tags.of("queue", queue), "Age in seconds of the oldest due task still waiting to be claimed")

    private fun probeGauge(state: String): AtomicLong = gauge(PROBES, Tags.of("state", state), "Probes by state; disabled probes are counted apart")

    private fun gauge(
        name: String,
        tags: Tags,
        description: String,
    ): AtomicLong =
        values.computeIfAbsent("$name$tags") { _ ->
            AtomicLong().also { value ->
                Gauge
                    .builder(name, value) { gauge -> gauge.get().toDouble() }
                    .description(description)
                    .tags(tags)
                    .register(registry)
            }
        }

    companion object {
        const val QUEUE_TASKS = "uptime.queue.tasks"
        const val QUEUE_LAG = "uptime.queue.lag.seconds"
        const val PROBES = "uptime.probes"
        const val QUEUE_PROBE_CHECKS = "probe_checks"
        const val QUEUE_NOTIFICATIONS = "notifications"
        const val STATE_DISABLED = "disabled"

        private val QUEUES = listOf(QUEUE_PROBE_CHECKS, QUEUE_NOTIFICATIONS)
        private val PROBE_STATES = ProbeMonitorLogStatus.entries.map { it.name.lowercase() } + STATE_DISABLED

        fun probeState(
            enabled: Boolean,
            status: ProbeMonitorLogStatus,
        ): String = if (enabled) status.name.lowercase() else STATE_DISABLED
    }
}
