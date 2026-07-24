package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.NotificationQueueDto
import tmenier.fr.databases.dtos.NotificationQueueRetryDto
import tmenier.fr.databases.dtos.UpdateNotificationQueueDto
import tmenier.fr.databases.entities.NotificationTaskEntity
import tmenier.fr.databases.mappers.NotificationTaskMapper
import java.util.UUID

@ApplicationScoped
class NotificationTaskRepository(
    private val em: EntityManager
) : PanacheRepositoryBase<NotificationTaskEntity, UUID> {

    @Transactional
        /** Retry send notification if failed */
    fun enqueueForRetry(retryDto: NotificationQueueRetryDto) {
        em.createNativeQuery(
            """
            INSERT INTO notification_tasks
                (probe_id, check_task_id, notification_id, status, channel, payload, event, attempt_count, next_attempt_at, error_message)
            VALUES (:probeId, :checkTaskId, :notificationId, 'pending', :channel, :payload, :event, 0, now() + interval '10 seconds', :errorMessage)
            ON CONFLICT (check_task_id) WHERE check_task_id IS NOT NULL DO NOTHING
            """
        ).setParameter("probeId", retryDto.probeId)
            .setParameter("checkTaskId", retryDto.taskId)
            .setParameter("notificationId", retryDto.notificationId)
            .setParameter("channel", retryDto.channel)
            .setParameter("payload", retryDto.payload)
            .setParameter("event", retryDto.event)
            .setParameter("errorMessage", retryDto.errorMessage)
            .executeUpdate()
    }

    /**
     * Réclame ET marque 'running' en une seule requête atomique -> aucun
     * risque de double-envoi même avec plusieurs workers qui pollent en même temps.
     */
    @Transactional
    fun claimDueTasks(workerId: String, limit: Int): List<UUID> {
        return em.createNativeQuery(
            """
            UPDATE notification_tasks
            SET status = 'running', claimed_by = :workerId, claimed_at = now()
            WHERE id IN (
                SELECT id FROM notification_tasks
                WHERE status = 'pending' AND next_attempt_at <= now()
                ORDER BY next_attempt_at
                FOR UPDATE SKIP LOCKED
                LIMIT :limit
            )
            RETURNING id
            """
        ).setParameter("workerId", workerId)
            .setParameter("limit", limit)
            .resultList as List<UUID>
    }

    @Transactional
    fun markSent(id: UUID) {
        update("status = 'sent' where id = ?1", id)
    }

    @Transactional
    fun markFailedAndReschedule(dto: UpdateNotificationQueueDto) {
        update(
            "status = ?1, attemptCount = ?2, errorMessage = ?3, nextAttemptAt = ?4 where id = ?5",
            dto.status,
            dto.attemptCount,
            dto.nextAttemptAt,
            dto.nextAttemptAt,
            dto.id,
            dto.errorMessage
        )
    }

    fun findByIdWithRelations(id: UUID): NotificationQueueDto {
        val entity = find(
            """
            SELECT t FROM NotificationTaskEntity t
            LEFT JOIN FETCH t.probe
            LEFT JOIN FETCH t.notification
            LEFT JOIN FETCH t.checkTask
            WHERE t.id ?1 :id
            """,
            id
        ).firstResult()
            ?: throw NotFoundException("No notification task found with id $id")

        return NotificationTaskMapper.toDtoWithRelation(entity)
    }

}
