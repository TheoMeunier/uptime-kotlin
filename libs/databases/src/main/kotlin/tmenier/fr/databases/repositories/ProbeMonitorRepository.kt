package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import tmenier.fr.databases.dtos.StoreProbeMonitorLogDto
import tmenier.fr.databases.entities.ProbesMonitorsLogEntity
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class ProbeMonitorRepository(
    private val probeRepository: ProbeRepository,
    private val em: EntityManager,
) : PanacheRepositoryBase<ProbesMonitorsLogEntity, UUID> {
    fun findByProbeAfter(
        probeId: UUID,
        after: Instant,
        limit: Int = MAX_POINTS,
    ): List<ProbesMonitorsLogEntity> =
        find(
            "probe.id = ?1 AND runAt > ?2 ORDER BY runAt DESC",
            probeId,
            after,
        ).range(0, limit - 1)
            .list()
            .reversed()

    fun findByProbe(probeId: UUID): List<ProbesMonitorsLogEntity> =
        find(
            "probe.id = ?1 ORDER BY runAt ASC",
            probeId,
        ).list()

    fun store(dto: StoreProbeMonitorLogDto) {
        val probe = probeRepository.findById(dto.probe.id)

        val entity = ProbesMonitorsLogEntity()
        entity.id = UUID.randomUUID()
        entity.runAt = dto.runAt
        entity.message = dto.message
        entity.status = dto.status
        entity.responseTime = dto.responseTime
        entity.probe = probe
        entity.checkTaskId = dto.checkTaskId
        entity.underMaintenance = dto.underMaintenance
        entity.persist()
    }

    fun deleteByProbe(probeId: UUID): Long = delete("probe.id = ?1", probeId)

    fun existsByCheckTaskId(checkTaskId: UUID): Boolean = count("checkTaskId = ?1", checkTaskId) > 0

    fun deleteExpiredBatch(
        now: Instant,
        rolledUpBefore: Instant,
        batchSize: Int,
    ): Int =
        em
            .createNativeQuery(
                """
                DELETE FROM probes_monitors_logs
                WHERE ctid IN (
                    SELECT l.ctid
                    FROM probes_monitors_logs l
                    JOIN probes p ON p.id = l.probe_id
                    CROSS JOIN instance_settings s
                    WHERE s.id = 1
                      AND COALESCE(p.log_retention_days, s.log_retention_days, 0) > 0
                      AND l.run_at < CAST(:now AS timestamptz)
                          - make_interval(secs => COALESCE(p.log_retention_days, s.log_retention_days) * 86400)
                      AND l.run_at < CAST(:rolledUpBefore AS timestamptz)
                    LIMIT :batch
                )
                """.trimIndent(),
            ).setParameter("now", now)
            .setParameter("rolledUpBefore", rolledUpBefore)
            .setParameter("batch", batchSize)
            .executeUpdate()

    fun countOlderThanForProbe(
        probeId: UUID,
        cutoff: Instant,
    ): Long = count("probe.id = ?1 AND runAt < ?2", probeId, cutoff)

    fun countOlderThanForInheritingProbes(cutoff: Instant): Long = count("probe.logRetentionDays IS NULL AND runAt < ?1", cutoff)

    fun countForProbe(probeId: UUID): Long = count("probe.id = ?1", probeId)

    fun countForInheritingProbes(): Long = count("probe.logRetentionDays IS NULL")

    fun oldestRunAtForProbe(probeId: UUID): Instant? =
        em
            .createQuery(
                "SELECT MIN(l.runAt) FROM ProbesMonitorsLogEntity l WHERE l.probe.id = :probeId",
                Instant::class.java,
            ).setParameter("probeId", probeId)
            .singleResult

    fun oldestRunAtForInheritingProbes(): Instant? =
        em
            .createQuery(
                "SELECT MIN(l.runAt) FROM ProbesMonitorsLogEntity l WHERE l.probe.logRetentionDays IS NULL",
                Instant::class.java,
            ).singleResult

    companion object {
        const val MAX_POINTS: Int = 500
    }
}
