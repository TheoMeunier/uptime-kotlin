package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.notifications.NotificationEvent
import tmenier.fr.common.enums.probes.QueueJobStatus
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.NotificationQueueDto
import tmenier.fr.databases.entities.NotificationTaskEntity
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.NotificationTaskMapper
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class NotificationTaskRepository(
    private val em: EntityManager,
) : PanacheRepositoryBase<NotificationTaskEntity, UUID> {
    fun enqueueDeliveries(
        probe: ProbesEntity,
        checkTaskId: UUID,
        result: ProbeResult,
        event: NotificationEvent,
        reminderIndex: Int = 0,
        downtime: Duration? = null,
    ) {
        if (event == NotificationEvent.NONE) return

        probe.notifications.forEach { notification ->
            val delivery =
                NotificationTaskEntity().apply {
                    id = UUID.randomUUID()
                    this.checkTaskId = checkTaskId
                    this.event = event
                    this.reminderIndex = reminderIndex
                    downtimeSeconds = downtime?.seconds
                    status = QueueJobStatus.PENDING
                    payload = NotificationTaskMapper.payloadToEntity(result)
                    nextAttemptAt = Instant.now()
                    this.notification = notification
                    this.probe = probe
                }
            em.persist(delivery)
        }
        bringForwardWaitingRetries(probe)
    }

    /**
     * A new state change must go out now, and it can only go out once the older deliveries of the
     * same probe have (see [claimDueTasks]). So the older ones still waiting for a retry are retried
     * right away rather than holding the new one behind their backoff.
     */
    private fun bringForwardWaitingRetries(probe: ProbesEntity) {
        em
            .createNativeQuery(
                """
            UPDATE notification_deliveries
            SET available_at = now()
            WHERE probe_id = :probeId
              AND status = 'PENDING'
              AND available_at > now()
            """,
            ).setParameter("probeId", probe.id)
            .executeUpdate()
    }

    @Transactional
    fun claimDueTasks(
        workerId: String,
        limit: Int,
        leaseDuration: Duration,
    ): List<UUID> {
        if (limit <= 0) return emptyList()

        @Suppress("UNCHECKED_CAST")
        return em
            .createNativeQuery(
                """
            UPDATE notification_deliveries
            SET status = 'LEASED',
                lease_owner = :workerId,
                lease_until = now() + make_interval(secs => :leaseSeconds),
                delivery_attempts = delivery_attempts + 1
            WHERE id IN (
                SELECT d.id
                FROM notification_deliveries d
                WHERE d.available_at <= now()
                  AND d.delivery_attempts < d.max_delivery_attempts
                  AND (
                      d.status = 'PENDING'
                      OR (d.status = 'LEASED' AND d.lease_until < now())
                  )
                  AND NOT EXISTS (
                      SELECT 1
                      FROM notification_deliveries older
                      WHERE older.probe_id = d.probe_id
                        AND older.notification_channel_id IS NOT DISTINCT FROM d.notification_channel_id
                        AND older.status IN ('PENDING', 'LEASED')
                        AND (older.created_at, older.id) < (d.created_at, d.id)
                  )
                ORDER BY d.available_at, d.created_at
                FOR UPDATE SKIP LOCKED
                LIMIT :limit
            )
            RETURNING id
            """,
            ).setParameter("workerId", workerId)
            .setParameter("leaseSeconds", leaseDuration.seconds)
            .setParameter("limit", limit)
            .resultList as List<UUID>
    }

    @Transactional
    fun markSent(id: UUID) {
        delete("id = ?1", id)
    }

    /**
     * Schedules the next attempt of a delivery that failed. A channel that answered "too many
     * requests" is retried when it asked to be, without spending an attempt, as long as the
     * delivery is still recent: a burst of alerts is not a broken channel.
     */
    @Transactional
    fun markFailedAndReschedule(
        id: UUID,
        errorMessage: String,
        retryAfter: Duration? = null,
    ) {
        val entity = findById(id) ?: return
        val now = Instant.now()
        entity.errorMessage = errorMessage
        entity.claimedBy = null
        entity.leaseUntil = null

        if (retryAfter != null && entity.createdAt.isAfter(now.minus(NotificationRetryDelay.RATE_LIMIT_PATIENCE))) {
            entity.attemptCount = (entity.attemptCount - 1).coerceAtLeast(0)
            entity.status = QueueJobStatus.PENDING
            entity.nextAttemptAt = now.plus(retryAfter)
            return
        }

        if (entity.attemptCount >= entity.maxAttempts) {
            entity.status = QueueJobStatus.DEAD
            return
        }

        entity.status = QueueJobStatus.PENDING
        entity.nextAttemptAt = now.plus(maxOf(NotificationRetryDelay.after(entity.attemptCount), retryAfter ?: Duration.ZERO))
    }

    @Transactional
    fun renewLeases(
        workerId: String,
        leaseDuration: Duration,
        deliveryIds: Collection<UUID>,
    ) {
        if (deliveryIds.isEmpty()) return
        em
            .createNativeQuery(
                """
            UPDATE notification_deliveries
            SET lease_until = now() + make_interval(secs => :leaseSeconds)
            WHERE status = 'LEASED'
              AND lease_owner = :workerId
              AND id IN (:ids)
            """,
            ).setParameter("leaseSeconds", leaseDuration.seconds)
            .setParameter("workerId", workerId)
            .setParameter("ids", deliveryIds)
            .executeUpdate()
    }

    @Transactional
    fun deadLetterExpiredLeases() {
        em
            .createNativeQuery(
                """
            UPDATE notification_deliveries
            SET status = 'DEAD',
                last_error = 'Lease expired after maximum delivery attempts',
                lease_owner = NULL,
                lease_until = NULL
            WHERE status = 'LEASED'
              AND lease_until < now()
              AND delivery_attempts >= max_delivery_attempts
            """,
            ).executeUpdate()
    }

    @Transactional
    fun findByIdWithRelations(id: UUID): NotificationQueueDto {
        val entity =
            find(
                """
                SELECT t FROM NotificationTaskEntity t
                JOIN FETCH t.probe
                JOIN FETCH t.notification
                WHERE t.id = ?1
                """,
                id,
            ).firstResult()
                ?: throw NotFoundException("No notification task found with id $id")

        return NotificationTaskMapper.toDtoWithRelation(entity)
    }
}
