package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.probes.QueueJobStatus
import tmenier.fr.databases.dtos.ProbeCheckTaskDto
import tmenier.fr.databases.dtos.StoreProbeCheckTaskDto
import tmenier.fr.databases.entities.ProbeCheckTaskEntity
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.ProbeCheckTaskMapper
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlin.math.max

@ApplicationScoped
class ProbeCheckTaskRepository(
    private val em: EntityManager,
) : PanacheRepositoryBase<ProbeCheckTaskEntity, UUID> {
    @Transactional
    fun ensureScheduledProbeChecks(
        defaultRegion: String,
        now: LocalDateTime = LocalDateTime.now(),
        limit: Int = 50,
    ): Int {
        val probes = lockProbesWithoutActiveTask(limit)
        var enqueued = 0

        probes.forEach { probe ->
            val scheduledAt = nextUnprocessedSchedule(probe, now)
            advanceAbsoluteSchedule(probe, scheduledAt, now)

            val configuredRegion = probe.regionsOrder.firstOrNull()?.asText()
            storeEntity(
                StoreProbeCheckTaskDto(
                    probeId = probe.id,
                    region = configuredRegion ?: defaultRegion,
                    attemptNumber = 1,
                    scheduleAt = scheduledAt,
                    availableAt = scheduledAt.atZone(ZoneId.systemDefault()).toInstant(),
                ),
            )
            enqueued++
        }

        return enqueued
    }

    @Transactional
    fun claimDueStandaloneProbes(
        now: LocalDateTime = LocalDateTime.now(),
        limit: Int = 50,
    ): List<UUID> {
        val probes = lockDueProbes(now, limit)
        probes.forEach { probe ->
            val scheduledAt = probe.nextCheckAt ?: now
            advanceAbsoluteSchedule(probe, scheduledAt, now)
        }
        return probes.map { it.id }
    }

    @Transactional
    fun claimPendingTasks(
        region: String,
        workerId: String,
        limit: Int,
        leaseDuration: Duration,
    ): List<ProbeCheckTaskDto> {
        if (limit <= 0) return emptyList()

        @Suppress("UNCHECKED_CAST")
        val ids =
            em
                .createNativeQuery(
                    """
                UPDATE probe_check_jobs
                SET status = 'LEASED',
                    lease_owner = :workerId,
                    lease_until = now() + make_interval(secs => :leaseSeconds),
                    delivery_attempts = delivery_attempts +
                        CASE WHEN status = 'LEASED' THEN 1 ELSE 0 END
                WHERE id IN (
                    SELECT id
                    FROM probe_check_jobs
                    WHERE region = :region
                      AND available_at <= now()
                      AND delivery_attempts < max_delivery_attempts
                      AND (
                          status = 'PENDING'
                          OR (
                              status = 'LEASED'
                              AND lease_until < now()
                              AND delivery_attempts + 1 < max_delivery_attempts
                          )
                      )
                    ORDER BY available_at, scheduled_at, created_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT :limit
                )
                RETURNING id
                """,
                ).setParameter("workerId", workerId)
                .setParameter("leaseSeconds", leaseDuration.seconds)
                .setParameter("region", region)
                .setParameter("limit", limit)
                .resultList as List<UUID>

        if (ids.isEmpty()) return emptyList()
        return find("id in ?1", ids)
            .list()
            .sortedWith(compareBy<ProbeCheckTaskEntity> { it.scheduledAt }.thenBy { it.createdAt })
            .map(ProbeCheckTaskMapper::toDto)
    }

    fun findByIdForUpdate(id: UUID): ProbeCheckTaskEntity? = em.find(ProbeCheckTaskEntity::class.java, id, LockModeType.PESSIMISTIC_WRITE)

    fun store(probeCheckTask: StoreProbeCheckTaskDto): ProbeCheckTaskEntity = storeEntity(probeCheckTask)

    fun deleteTask(task: ProbeCheckTaskEntity) {
        em.remove(task)
        em.flush()
    }

    @Transactional
    fun markTechnicalFailure(
        taskId: UUID,
        workerId: String,
        message: String,
        now: Instant = Instant.now(),
    ) {
        val task = findByIdForUpdate(taskId) ?: return
        if (task.status != QueueJobStatus.LEASED || task.claimedBy != workerId) return
        val probe = em.find(ProbesEntity::class.java, task.probeId)
        if (probe == null || !probe.enabled) {
            deleteTask(task)
            return
        }

        val nextAttempt = task.deliveryAttempts + 1
        task.deliveryAttempts = nextAttempt
        task.resultMessage = message
        task.claimedBy = null
        task.leaseUntil = null

        if (nextAttempt >= task.maxDeliveryAttempts) {
            task.status = QueueJobStatus.DEAD
            return
        }

        val backoffSeconds = 5L * (1L shl nextAttempt.coerceAtMost(6))
        task.status = QueueJobStatus.PENDING
        task.availableAt = now.plusSeconds(backoffSeconds)
    }

    @Transactional
    fun deadLetterExpiredLeases() {
        em
            .createNativeQuery(
                """
            UPDATE probe_check_jobs
            SET status = 'DEAD',
                last_error = 'Lease expired after maximum technical delivery attempts',
                lease_owner = NULL,
                lease_until = NULL
            WHERE status = 'LEASED'
              AND lease_until < now()
              AND delivery_attempts + 1 >= max_delivery_attempts
            """,
            ).executeUpdate()
    }

    @Transactional
    fun renewLeases(
        workerId: String,
        leaseDuration: Duration,
        taskIds: Collection<UUID>,
    ) {
        if (taskIds.isEmpty()) return
        update(
            "leaseUntil = ?1 where status = ?2 and claimedBy = ?3 and id in ?4",
            Instant.now().plus(leaseDuration),
            QueueJobStatus.LEASED,
            workerId,
            taskIds,
        )
    }

    @Transactional
    fun cancelPending(probeId: UUID) {
        delete("probeId = ?1 and status = ?2", probeId, QueueJobStatus.PENDING)
    }

    @Transactional
    fun reschedulePendingRetries(
        probeId: UUID,
        intervalRetrySeconds: Int,
    ) {
        em
            .createNativeQuery(
                """
            UPDATE probe_check_jobs
            SET available_at = greatest(
                now(),
                previous_failed_at + make_interval(secs => :intervalSeconds)
            )
            WHERE probe_id = :probeId
              AND status = 'PENDING'
              AND probe_attempt > 1
              AND previous_failed_at IS NOT NULL
            """,
            ).setParameter("intervalSeconds", intervalRetrySeconds)
            .setParameter("probeId", probeId)
            .executeUpdate()
    }

    private fun storeEntity(dto: StoreProbeCheckTaskDto): ProbeCheckTaskEntity {
        val entity =
            ProbeCheckTaskEntity().apply {
                id = UUID.randomUUID()
                probeId = dto.probeId
                region = dto.region
                attemptNumber = dto.attemptNumber
                scheduledAt = dto.scheduleAt
                availableAt = dto.availableAt
                previousFailedAt = dto.previousFailedAt
            }
        em.persist(entity)
        return entity
    }

    private fun lockDueProbes(
        now: LocalDateTime,
        limit: Int,
    ): List<ProbesEntity> {
        @Suppress("UNCHECKED_CAST")
        val ids =
            em
                .createNativeQuery(
                    """
                SELECT id
                FROM probes
                WHERE enabled = true
                  AND (next_check_at IS NULL OR next_check_at <= :now)
                ORDER BY next_check_at ASC NULLS FIRST
                FOR UPDATE SKIP LOCKED
                LIMIT :limit
                """,
                ).setParameter("now", now)
                .setParameter("limit", limit)
                .resultList as List<UUID>

        return ids.map { em.find(ProbesEntity::class.java, it) }
    }

    private fun lockProbesWithoutActiveTask(limit: Int): List<ProbesEntity> {
        @Suppress("UNCHECKED_CAST")
        val ids =
            em
                .createNativeQuery(
                    """
                SELECT p.id
                FROM probes p
                WHERE p.enabled = true
                  AND NOT EXISTS (
                      SELECT 1
                      FROM probe_check_jobs j
                      WHERE j.probe_id = p.id
                        AND j.status IN ('PENDING', 'LEASED')
                  )
                ORDER BY p.next_check_at ASC NULLS FIRST
                FOR UPDATE OF p SKIP LOCKED
                LIMIT :limit
                """,
                ).setParameter("limit", limit)
                .resultList as List<UUID>

        return ids.map { em.find(ProbesEntity::class.java, it) }
    }

    private fun nextUnprocessedSchedule(
        probe: ProbesEntity,
        fallback: LocalDateTime,
    ): LocalDateTime {
        val intervalSeconds = max(1, probe.interval).toLong()
        val lastRun = probe.lastRun ?: return probe.nextCheckAt ?: fallback
        var scheduledAt = probe.nextCheckAt ?: fallback

        while (!scheduledAt.isAfter(lastRun)) {
            scheduledAt = scheduledAt.plusSeconds(intervalSeconds)
        }

        return scheduledAt
    }

    private fun advanceAbsoluteSchedule(
        probe: ProbesEntity,
        scheduledAt: LocalDateTime,
        now: LocalDateTime,
    ) {
        val intervalSeconds = max(1, probe.interval).toLong()
        var next = scheduledAt.plusSeconds(intervalSeconds)
        while (!next.isAfter(now)) {
            next = next.plusSeconds(intervalSeconds)
        }
        probe.nextCheckAt = next
    }
}
