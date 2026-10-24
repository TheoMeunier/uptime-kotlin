package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.probes.QueueJobStatus
import tmenier.fr.databases.entities.WorkerHeartbeat
import java.time.Duration
import java.time.Instant

@ApplicationScoped
class OperationalMetricsRepository(
    private val em: EntityManager,
) {
    @Transactional
    fun probeCheckJobsByStatus(): Map<QueueJobStatus, Long> = countByStatus("ProbeCheckTaskEntity")

    @Transactional
    fun notificationDeliveriesByStatus(): Map<QueueJobStatus, Long> = countByStatus("NotificationTaskEntity")

    @Transactional
    fun probeCheckJobsLag(now: Instant = Instant.now()): Duration = lag("ProbeCheckTaskEntity", "availableAt", now)

    @Transactional
    fun notificationDeliveriesLag(now: Instant = Instant.now()): Duration = lag("NotificationTaskEntity", "nextAttemptAt", now)

    @Transactional
    fun workerHeartbeats(): List<WorkerHeartbeat> =
        em
            .createQuery("select h from WorkerHeartbeat h", WorkerHeartbeat::class.java)
            .resultList

    @Transactional
    fun enabledProbeIntervals(): List<Int> =
        em
            .createQuery("select p.interval from ProbesEntity p where p.enabled = true", Int::class.javaObjectType)
            .resultList

    @Transactional
    fun probesByState(): List<ProbeStateCount> =
        em
            .createQuery(
                "select p.enabled, p.status, count(p) from ProbesEntity p group by p.enabled, p.status",
                Array<Any>::class.java,
            ).resultList
            .map { row ->
                ProbeStateCount(
                    enabled = row[0] as Boolean,
                    status = row[1] as ProbeMonitorLogStatus,
                    count = (row[2] as Number).toLong(),
                )
            }

    private fun lag(
        entity: String,
        availableAt: String,
        now: Instant,
    ): Duration {
        val oldest: Instant? =
            em
                .createQuery(
                    "select min(t.$availableAt) from $entity t where t.status = :pending and t.$availableAt <= :now",
                    Instant::class.java,
                ).setParameter("pending", QueueJobStatus.PENDING)
                .setParameter("now", now)
                .singleResult
        return if (oldest == null) Duration.ZERO else Duration.between(oldest, now)
    }

    private fun countByStatus(entity: String): Map<QueueJobStatus, Long> =
        em
            .createQuery("select t.status, count(t) from $entity t group by t.status", Array<Any>::class.java)
            .resultList
            .associate { row -> row[0] as QueueJobStatus to (row[1] as Number).toLong() }
}

data class ProbeStateCount(
    val enabled: Boolean,
    val status: ProbeMonitorLogStatus,
    val count: Long,
)
