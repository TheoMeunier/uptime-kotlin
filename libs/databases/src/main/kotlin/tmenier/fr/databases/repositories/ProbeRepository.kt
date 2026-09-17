package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import jakarta.persistence.Tuple
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.dtos.ProbeOnOffDto
import tmenier.fr.databases.dtos.ProbeStatusDTO
import tmenier.fr.databases.dtos.ProbeStatusMetrics
import tmenier.fr.databases.dtos.ProbeUptimeDTO
import tmenier.fr.databases.dtos.ProbesStatusFingerprint
import tmenier.fr.databases.dtos.StoreProbeDto
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.ProbeContentMapper
import tmenier.fr.databases.mappers.ProbeMapper
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class ProbeRepository(
    private val notificationRepository: NotificationRepository,
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
    private val em: EntityManager,
) : PanacheRepositoryBase<ProbesEntity, UUID> {
    @Transactional
    override fun findById(id: UUID): ProbesEntity = find("id = ?1", id).firstResult() ?: throw IllegalArgumentException("Probe not found")

    fun findByIdOrNull(id: UUID): ProbesEntity? = find("id = ?1", id).firstResult()

    fun findByIdWithNotificationsOrNull(id: UUID): ProbesEntity? =
        find(
            "SELECT p FROM ProbesEntity p LEFT JOIN FETCH p.notifications WHERE p.id = ?1",
            id,
        ).firstResult()

    fun findByIdForUpdate(id: UUID): ProbesEntity =
        em.find(ProbesEntity::class.java, id, LockModeType.PESSIMISTIC_WRITE)
            ?: throw IllegalArgumentException("Probe not found")

    fun findByIds(ids: List<UUID>): List<ProbesEntity> = find("id in ?1", ids).list()

    fun getProbesLastHourWithMetrics(metrics: Map<UUID, ProbeStatusMetrics>): List<ProbeStatusDTO> =
        findProbesWithLastHourLogs().map { ProbeMapper.toStatusDto(it, metrics[it.id]) }

    private fun findProbesWithLastHourLogs(): List<ProbesEntity> =
        find(
            "SELECT DISTINCT p FROM ProbesEntity p JOIN FETCH p.probesMonitorLogs pml WHERE pml.runAt > ?1 AND p.enabled = true ORDER BY p.name ASC",
            LocalDateTime.now().minusHours(1),
        ).list().sortedBy { it.name.lowercase() }

    fun getProbesStatusMetrics(): Map<UUID, ProbeStatusMetrics> {
        val now = LocalDateTime.now()
        val since24h = now.minusHours(24)
        val since7d = now.minusDays(7)
        val since30d = now.minusDays(30)

        val jpql =
            """
            SELECT p.id,
              SUM(CASE WHEN pml.runAt > :since24h THEN 1 ELSE 0 END),
              SUM(CASE WHEN pml.runAt > :since24h AND pml.status = :success THEN 1 ELSE 0 END),
              SUM(CASE WHEN pml.runAt > :since7d THEN 1 ELSE 0 END),
              SUM(CASE WHEN pml.runAt > :since7d AND pml.status = :success THEN 1 ELSE 0 END),
              SUM(CASE WHEN pml.id IS NOT NULL THEN 1 ELSE 0 END),
              SUM(CASE WHEN pml.status = :success THEN 1 ELSE 0 END),
              MAX(CASE WHEN pml.status = :success THEN pml.runAt ELSE NULL END)
            FROM ProbesEntity p
            LEFT JOIN p.probesMonitorLogs pml WITH pml.runAt > :since30d AND pml.underMaintenance = false
            WHERE p.enabled = true
            GROUP BY p.id
            """.trimIndent()

        val rows =
            em
                .createQuery(jpql, Tuple::class.java)
                .setParameter("since24h", since24h)
                .setParameter("since7d", since7d)
                .setParameter("since30d", since30d)
                .setParameter("success", ProbeMonitorLogStatus.SUCCESS)
                .resultList

        return rows.associate { row ->
            val probeId = row[0] as UUID

            probeId to
                ProbeStatusMetrics(
                    probeId = probeId,
                    uptimes =
                        ProbeUptimeDTO(
                            h24 = ratio(row[2], row[1]),
                            d7 = ratio(row[4], row[3]),
                            d30 = ratio(row[6], row[5]),
                        ),
                    lastSuccessAt = row[7] as LocalDateTime?,
                )
        }
    }

    fun getStatusFingerprint(since: LocalDateTime): ProbesStatusFingerprint {
        val probes =
            em
                .createQuery(
                    """
                    SELECT COUNT(p.id),
                      SUM(CASE WHEN p.status = :failure THEN 1 ELSE 0 END),
                      MAX(p.updatedAt)
                    FROM ProbesEntity p
                    WHERE p.enabled = true
                    """.trimIndent(),
                    Tuple::class.java,
                ).setParameter("failure", ProbeMonitorLogStatus.FAILURE)
                .singleResult

        val logs =
            em
                .createQuery(
                    """
                    SELECT COUNT(pml.id), MAX(pml.runAt)
                    FROM ProbesMonitorsLogEntity pml
                    WHERE pml.probe.enabled = true AND pml.runAt > :since
                    """.trimIndent(),
                    Tuple::class.java,
                ).setParameter("since", since)
                .singleResult

        return ProbesStatusFingerprint(
            enabledProbes = asLong(probes[0]),
            failingProbes = asLong(probes[1]),
            lastProbeUpdateAt = probes[2] as LocalDateTime?,
            logsInWindow = asLong(logs[0]),
            lastLogAt = logs[1] as LocalDateTime?,
        )
    }

    private fun asLong(value: Any?): Long = ((value as Number?) ?: 0L).toLong()

    private fun ratio(
        success: Any?,
        total: Any?,
    ): Double {
        val totalCount = ((total as Number?) ?: 0L).toLong()
        if (totalCount == 0L) return 100.0

        val successCount = ((success as Number?) ?: 0L).toLong()
        return (successCount.toDouble() / totalCount.toDouble()) * 100.0
    }

    fun getActiveProbes(): List<ProbeDTO> = find("enabled = ?1 ORDER BY name ASC", true).list().map { ProbeMapper.toDto(it) }

    fun getAll(): List<ProbesEntity> = findAll(Sort.by("name")).list()

    fun attach(
        notifications: List<UUID>,
        probe: ProbesEntity,
    ) {
        val notificationsEntities = notificationRepository.findByIds(notifications)
        probe.notifications.addAll(notificationsEntities)
    }

    fun delete(probeId: UUID) = delete("id = ?1", probeId)

    fun save(
        dto: StoreProbeDto,
        notifications: List<UUID>,
    ) {
        val entity = ProbeMapper.toEntity(dto)
        attach(notifications, entity)
        entity.persist()
    }

    fun update(
        dto: StoreProbeDto,
        notifications: List<UUID>,
    ) {
        val entity = findById(dto.id)
        entity.name = dto.name
        entity.interval = dto.interval
        entity.intervalRetry = dto.intervalRetry
        entity.retry = dto.retry
        entity.protocol = dto.protocol
        entity.enabled = dto.enabled
        entity.description = dto.description
        entity.content = ProbeContentMapper.toEntity(dto.content).first
        applyAlertRepeat(entity, dto.alertRepeatSeconds)

        entity.notifications.clear()
        attach(notifications, entity)

        entity.persist()
        probeCheckTaskRepository.reschedulePendingRetries(dto.id, dto.intervalRetry)
    }

    fun updateStatus(
        probeId: UUID,
        status: ProbeMonitorLogStatus,
    ) {
        val entity = findById(probeId)
        entity.status = status
        entity.updatedAt = LocalDateTime.now()
        entity.persist()
    }

    /**
     * Keeps the resend deadline consistent with the setting it depends on: turning
     * the resend off must silence the probe now, and turning it on while the probe
     * is already announced as down must arm a deadline -- otherwise the setting
     * would only take effect at the next outage.
     */
    private fun applyAlertRepeat(
        entity: ProbesEntity,
        alertRepeatSeconds: Int,
    ) {
        entity.alertRepeatSeconds = alertRepeatSeconds

        if (alertRepeatSeconds <= 0) {
            entity.nextAlertAt = null
            entity.alertRepeatCount = 0
            return
        }

        if (entity.alertedStatus == ProbeMonitorLogStatus.FAILURE && entity.nextAlertAt == null) {
            entity.nextAlertAt = Instant.now().plusSeconds(alertRepeatSeconds.toLong())
        }
    }

    fun onOff(dto: ProbeOnOffDto) {
        val probe = findByIdForUpdate(dto.id)
        probe.enabled = dto.enabled
        probe.status = dto.status
        probe.alertedStatus = dto.status
        // A paused probe has no open alert: a deadline left behind would fire on
        // the first check after it is resumed.
        probe.nextAlertAt = null
        probe.alertRepeatCount = 0
        probe.nextCheckAt = if (dto.enabled) LocalDateTime.now() else null
        if (!dto.enabled) {
            probeCheckTaskRepository.cancelPending(dto.id)
        }
        probe.persist()
    }
}
