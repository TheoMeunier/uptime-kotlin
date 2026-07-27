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
    ) {
        if (event == NotificationEvent.NONE) return

        probe.notifications.forEach { notification ->
            val delivery =
                NotificationTaskEntity().apply {
                    id = UUID.randomUUID()
                    this.checkTaskId = checkTaskId
                    this.event = event
                    status = QueueJobStatus.PENDING
                    payload = NotificationTaskMapper.payloadToEntity(result)
                    nextAttemptAt = Instant.now()
                    this.notification = notification
                    this.probe = probe
                }
            em.persist(delivery)
        }
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
                SELECT id
                FROM notification_deliveries
                WHERE available_at <= now()
                  AND delivery_attempts < max_delivery_attempts
                  AND (
                      status = 'PENDING'
                      OR (status = 'LEASED' AND lease_until < now())
                  )
                ORDER BY available_at, created_at
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

    @Transactional
    fun markFailedAndReschedule(
        id: UUID,
        errorMessage: String,
        baseBackoffSeconds: Long = 10,
    ) {
        val entity = findById(id) ?: return
        entity.errorMessage = errorMessage
        entity.claimedBy = null
        entity.leaseUntil = null

        if (entity.attemptCount >= entity.maxAttempts) {
            entity.status = QueueJobStatus.DEAD
            return
        }

        val backoff = baseBackoffSeconds * (1L shl entity.attemptCount.coerceAtMost(6))
        entity.status = QueueJobStatus.PENDING
        entity.nextAttemptAt = Instant.now().plusSeconds(backoff)
    }

    @Transactional
    fun renewLeases(
        workerId: String,
        leaseDuration: Duration,
        deliveryIds: Collection<UUID>,
    ) {
        if (deliveryIds.isEmpty()) return
        update(
            "leaseUntil = ?1 where status = ?2 and claimedBy = ?3 and id in ?4",
            Instant.now().plus(leaseDuration),
            QueueJobStatus.LEASED,
            workerId,
            deliveryIds,
        )
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
