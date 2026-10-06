package tmenier.fr.notifications.jobs

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal class DeliveryWatchdog(
    private val maxDuration: Duration,
    private val clock: Clock = Clock.systemUTC(),
) {
    private class Running(
        val startedAt: Instant,
        val worker: Thread,
    ) {
        var interrupted = false
    }

    private val running = ConcurrentHashMap<UUID, Running>()

    fun started(id: UUID) {
        running[id] = Running(clock.instant(), Thread.currentThread())
    }

    fun finished(id: UUID) {
        running.computeIfPresent(id) { _, _ -> null }
    }

    fun renewable(): Set<UUID> {
        val now = clock.instant()
        return running.filterValues { !isOverdue(it, now) }.keys.toSet()
    }

    fun interruptOverdue(): List<UUID> {
        val now = clock.instant()
        val interrupted = mutableListOf<UUID>()
        running.keys.forEach { id ->
            running.computeIfPresent(id) { _, delivery ->
                if (!delivery.interrupted && isOverdue(delivery, now)) {
                    delivery.interrupted = true
                    delivery.worker.interrupt()
                    interrupted += id
                }
                delivery
            }
        }
        return interrupted
    }

    private fun isOverdue(
        delivery: Running,
        now: Instant,
    ): Boolean = Duration.between(delivery.startedAt, now) > maxDuration
}
