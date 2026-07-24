package tmenier.fr.databases.repositories

import ProbeCheckTaskStatusEnum
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.ProbeCheckTaskDto
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.dtos.StoreProbeCheckTaskDto
import tmenier.fr.databases.entities.ProbeCheckTaskEntity
import tmenier.fr.databases.mappers.ProbeCheckTaskMapper
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class ProbeCheckTaskRepository(
    private val em: EntityManager,
    private val probeRepository: ProbeRepository
) : PanacheRepositoryBase<ProbeCheckTaskEntity, UUID> {

    @Transactional
    fun claimPendingTasks(region: String, workerId: String, limit: Int = 10): List<ProbeCheckTaskDto>? {
        val candidateIds = em.createNativeQuery(
            """
        SELECT id FROM (
            SELECT DISTINCT ON (probe_id) id, probe_id, scheduled_at
            FROM probe_check_tasks
            WHERE region = :region
              AND status = 'PENDING'
              AND scheduled_at <= now()
              AND NOT EXISTS (
                  SELECT 1 FROM probe_check_tasks running
                  WHERE running.probe_id = probe_check_tasks.probe_id
                    AND running.status = 'RUNNING'
              )
            ORDER BY probe_id, scheduled_at
        ) t
        ORDER BY scheduled_at
        LIMIT :limit
        """
        ).setParameter("region", region)
            .setParameter("limit", limit)
            .resultList as List<*>

        if (candidateIds.isEmpty()) return null

        val lockedIds = em.createNativeQuery(
            """
        SELECT id FROM probe_check_tasks
        WHERE id IN (:ids)
        FOR UPDATE SKIP LOCKED
        """
        ).setParameter("ids", candidateIds)
            .resultList as List<*>

        if (lockedIds.isEmpty()) return null

        val tasks = find("id in ?1", lockedIds).list()

        tasks.forEach {
            it.status = ProbeCheckTaskStatusEnum.RUNNING
            it.claimedBy = workerId
            it.claimedAt = Instant.now()
        }

        return tasks.map { ProbeCheckTaskMapper.toDto(it) }
    }

    @Transactional
    fun markSuccess(task: ProbeCheckTaskDto, message: String?) {
        val taskEntity = find("id = ?1", task.id).firstResult() ?: throw IllegalArgumentException("Task not found")

        taskEntity.status = ProbeCheckTaskStatusEnum.SUCCESS
        taskEntity.resultMessage = message

        em.createNativeQuery(
            """
            UPDATE probe_check_tasks SET status = 'CANCELLED'
            WHERE probe_id = :probeId AND status = 'PENDING'
            """
        ).setParameter("probeId", task.probeId).executeUpdate()

        taskEntity.persist()
    }

    @Transactional
    fun markFailedAndMaybeCascade(task: ProbeCheckTaskDto, message: String?, probe: ProbeDTO) {
        val taskEntity = find("id = ?1", task.id).firstResult() ?: throw IllegalArgumentException("Task not found")
        taskEntity.status = ProbeCheckTaskStatusEnum.FAILED
        taskEntity.resultMessage = message
        taskEntity.persist()

        val regions = probe.regionsOrder?.toList() ?: emptyList()
        val nextIndex = task.attemptNumber // 0-based: attempt 1 => index 1 = 2e région

        if (nextIndex < regions.size && task.attemptNumber < probe.retry) {
            val next = ProbeCheckTaskEntity()
            next.id = UUID.randomUUID()
            next.probeId = probe.id
            next.region = regions[nextIndex]
            next.attemptNumber = task.attemptNumber + 1
            next.scheduledAt = LocalDateTime.ofInstant(
                Instant.now().plusSeconds(probe.intervalRetry.toLong()),
                java.time.ZoneId.systemDefault()
            )
            next.persist()
        } else {
            probeRepository.updateStatus(probe.id, ProbeMonitorLogStatus.FAILURE)
        }
    }

    @Transactional
    fun store(probeCheckTask: StoreProbeCheckTaskDto): ProbeCheckTaskEntity {
        val entity = ProbeCheckTaskEntity()
        entity.id = UUID.randomUUID()
        entity.probeId = probeCheckTask.probeId
        entity.region = probeCheckTask.region
        entity.attemptNumber = probeCheckTask.attemptNumber
        entity.scheduledAt = probeCheckTask.scheduleAt
        entity.persist()
        return entity
    }

    @Transactional
    fun releaseStale(maxRunningDuration: Duration) {
        em.createNativeQuery(
            """
        UPDATE probe_check_tasks
        SET status = 'FAILED', result_message = 'Auto-reset: stale running task (worker likely crashed or restarted)'
        WHERE status = 'RUNNING'
          AND claimed_at < now() - make_interval(secs => :seconds)
        """
        ).setParameter("seconds", maxRunningDuration.seconds)
            .executeUpdate()
    }
}
