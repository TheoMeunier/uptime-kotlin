package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import tmenier.fr.databases.entities.WorkerHeartbeat
import java.time.Duration
import java.time.Instant

@ApplicationScoped
class WorkerHeartbeatRepository(
    private val em: EntityManager,
) : PanacheRepositoryBase<WorkerHeartbeat, String> {
    @Transactional
    fun beat(
        workerId: String,
        region: String,
        now: Instant = Instant.now(),
    ) {
        em
            .createNativeQuery(
                """
                INSERT INTO worker_heartbeats (worker_id, region, last_seen_at)
                VALUES (:workerId, :region, :now)
                ON CONFLICT (worker_id)
                DO UPDATE SET region = EXCLUDED.region, last_seen_at = EXCLUDED.last_seen_at
                """.trimIndent(),
            ).setParameter("workerId", workerId)
            .setParameter("region", region)
            .setParameter("now", now)
            .executeUpdate()
    }

    @Transactional
    fun activeWorkerCount(freshness: Duration = ACTIVE_WINDOW): Int = count("lastSeenAt > ?1", Instant.now().minus(freshness)).toInt()

    @Transactional
    fun activeWorkerCount(
        region: String,
        freshness: Duration = ACTIVE_WINDOW,
    ): Int = count("region = ?1 and lastSeenAt > ?2", region, Instant.now().minus(freshness)).toInt()

    fun activeRegions(freshness: Duration = ACTIVE_WINDOW): List<String> =
        em
            .createQuery(
                "select distinct h.region from WorkerHeartbeat h where h.lastSeenAt > :since order by h.region",
                String::class.java,
            ).setParameter("since", Instant.now().minus(freshness))
            .resultList

    @Transactional
    fun purgeStale(olderThan: Duration = STALE_RETENTION): Long = delete("lastSeenAt < ?1", Instant.now().minus(olderThan))

    companion object {
        val ACTIVE_WINDOW: Duration = Duration.ofSeconds(30)
        val STALE_RETENTION: Duration = Duration.ofHours(1)
    }
}
