package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.UptimeBucketRow
import tmenier.fr.databases.dtos.UptimeCounts
import tmenier.fr.databases.support.RollupSplit
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@ApplicationScoped
class ProbeLogRollupRepository(
    private val em: EntityManager,
) {
    fun watermark(): Instant? =
        em
            .createNativeQuery("SELECT rolled_up_until FROM probe_log_rollup_state WHERE id = 1")
            .resultList
            .firstOrNull()
            ?.let(::toInstant)

    fun advanceWatermark(until: Instant): Int =
        em
            .createNativeQuery(
                """
                UPDATE probe_log_rollup_state
                SET rolled_up_until = GREATEST(rolled_up_until, CAST(:until AS timestamptz)),
                    updated_at      = NOW()
                WHERE id = 1
                """.trimIndent(),
            ).setParameter("until", until)
            .executeUpdate()

    fun oldestRawRunAt(): Instant? =
        em
            .createQuery("SELECT MIN(l.runAt) FROM ProbesMonitorsLogEntity l", Instant::class.java)
            .singleResult

    fun rollUp(
        from: Instant,
        to: Instant,
    ): Int =
        em
            .createNativeQuery(
                """
                INSERT INTO probe_logs_hourly (probe_id, bucket_start,
                                               checks_total, checks_success, checks_warning, checks_failure,
                                               checks_maintenance,
                                               response_time_sum, response_time_count, response_time_min,
                                               response_time_max, last_success_at, computed_at)
                SELECT l.probe_id,
                       date_trunc('hour', l.run_at, 'UTC'),
                       COUNT(*) FILTER (WHERE NOT l.under_maintenance),
                       COUNT(*) FILTER (WHERE NOT l.under_maintenance AND l.status = :success),
                       COUNT(*) FILTER (WHERE NOT l.under_maintenance AND l.status = :warning),
                       COUNT(*) FILTER (WHERE NOT l.under_maintenance AND l.status = :failure),
                       COUNT(*) FILTER (WHERE l.under_maintenance),
                       COALESCE(SUM(l.response_time) FILTER (WHERE l.status IN (:success, :warning)), 0),
                       COUNT(*) FILTER (WHERE l.status IN (:success, :warning)),
                       MIN(l.response_time) FILTER (WHERE l.status IN (:success, :warning)),
                       MAX(l.response_time) FILTER (WHERE l.status IN (:success, :warning)),
                       MAX(l.run_at) FILTER (WHERE NOT l.under_maintenance AND l.status = :success),
                       NOW()
                FROM probes_monitors_logs l
                WHERE l.run_at >= CAST(:from AS timestamptz)
                  AND l.run_at < CAST(:to AS timestamptz)
                GROUP BY l.probe_id, date_trunc('hour', l.run_at, 'UTC')
                ON CONFLICT (probe_id, bucket_start) DO UPDATE
                    SET checks_total        = EXCLUDED.checks_total,
                        checks_success      = EXCLUDED.checks_success,
                        checks_warning      = EXCLUDED.checks_warning,
                        checks_failure      = EXCLUDED.checks_failure,
                        checks_maintenance  = EXCLUDED.checks_maintenance,
                        response_time_sum   = EXCLUDED.response_time_sum,
                        response_time_count = EXCLUDED.response_time_count,
                        response_time_min   = EXCLUDED.response_time_min,
                        response_time_max   = EXCLUDED.response_time_max,
                        last_success_at     = EXCLUDED.last_success_at,
                        computed_at         = EXCLUDED.computed_at
                """.trimIndent(),
            ).setParameter("from", from)
            .setParameter("to", to)
            .withStatuses()
            .executeUpdate()

    fun uptimeCounts(
        probeIds: Collection<UUID>,
        from: Instant,
        to: Instant,
        watermark: Instant? = watermark(),
    ): Map<UUID, UptimeCounts> {
        if (probeIds.isEmpty()) return emptyMap()
        val split = RollupSplit.of(from, to, watermark)

        @Suppress("UNCHECKED_CAST")
        val rows =
            em
                .createNativeQuery(
                    """
                    SELECT s.probe_id, SUM(s.total), SUM(s.successes), MAX(s.last_success_at)
                    FROM (
                        SELECT h.probe_id,
                               h.checks_total    AS total,
                               h.checks_success  AS successes,
                               h.last_success_at AS last_success_at
                        FROM probe_logs_hourly h
                        WHERE h.probe_id IN (:probeIds)
                          AND h.bucket_start >= CAST(:rollupStart AS timestamptz)
                          AND h.bucket_start < CAST(:rollupEnd AS timestamptz)
                        UNION ALL
                        SELECT l.probe_id,
                               CASE WHEN NOT l.under_maintenance THEN 1 ELSE 0 END,
                               CASE WHEN NOT l.under_maintenance AND l.status = :success THEN 1 ELSE 0 END,
                               CASE WHEN NOT l.under_maintenance AND l.status = :success THEN l.run_at END
                        FROM probes_monitors_logs l
                        WHERE l.probe_id IN (:probeIds)
                          AND l.run_at >= CAST(:from AS timestamptz)
                          AND l.run_at <= CAST(:to AS timestamptz)
                          AND (l.run_at < CAST(:rollupStart AS timestamptz) OR l.run_at >= CAST(:rollupEnd AS timestamptz))
                    ) s
                    GROUP BY s.probe_id
                    """.trimIndent(),
                ).setParameter("probeIds", probeIds)
                .withSplit(split)
                .setParameter("success", ProbeMonitorLogStatus.SUCCESS.ordinal)
                .resultList as List<Array<Any?>>

        return rows.associate { row ->
            val probeId = row[0] as UUID
            probeId to
                UptimeCounts(
                    total = (row[1] as Number).toLong(),
                    successes = (row[2] as Number).toLong(),
                    lastSuccessAt = row[3]?.let(::toInstant),
                )
        }
    }

    fun uptimeBuckets(
        probeIds: Collection<UUID>,
        from: Instant,
        to: Instant,
        zone: ZoneId,
        bucket: Duration,
        watermark: Instant? = watermark(),
    ): List<UptimeBucketRow> {
        if (probeIds.isEmpty()) return emptyList()
        val split = RollupSplit.of(from, to, watermark)

        @Suppress("UNCHECKED_CAST")
        val rows =
            em
                .createNativeQuery(
                    """
                    SELECT s.probe_id, s.bucket,
                           SUM(s.total), SUM(s.successes), SUM(s.failures), SUM(s.warnings), SUM(s.maintenance)
                    FROM (
                        SELECT h.probe_id,
                               date_bin(make_interval(secs => :bucketSeconds), h.bucket_start AT TIME ZONE :zone,
                                        TIMESTAMP '2000-01-01') AS bucket,
                               h.checks_total       AS total,
                               h.checks_success     AS successes,
                               h.checks_failure     AS failures,
                               h.checks_warning     AS warnings,
                               h.checks_maintenance AS maintenance
                        FROM probe_logs_hourly h
                        WHERE h.probe_id IN (:probeIds)
                          AND h.bucket_start >= CAST(:rollupStart AS timestamptz)
                          AND h.bucket_start < CAST(:rollupEnd AS timestamptz)
                        UNION ALL
                        SELECT l.probe_id,
                               date_bin(make_interval(secs => :bucketSeconds), l.run_at AT TIME ZONE :zone,
                                        TIMESTAMP '2000-01-01'),
                               CASE WHEN NOT l.under_maintenance THEN 1 ELSE 0 END,
                               CASE WHEN NOT l.under_maintenance AND l.status = :success THEN 1 ELSE 0 END,
                               CASE WHEN NOT l.under_maintenance AND l.status = :failure THEN 1 ELSE 0 END,
                               CASE WHEN NOT l.under_maintenance AND l.status = :warning THEN 1 ELSE 0 END,
                               CASE WHEN l.under_maintenance THEN 1 ELSE 0 END
                        FROM probes_monitors_logs l
                        WHERE l.probe_id IN (:probeIds)
                          AND l.run_at >= CAST(:from AS timestamptz)
                          AND l.run_at <= CAST(:to AS timestamptz)
                          AND (l.run_at < CAST(:rollupStart AS timestamptz) OR l.run_at >= CAST(:rollupEnd AS timestamptz))
                    ) s
                    GROUP BY s.probe_id, s.bucket
                    """.trimIndent(),
                ).setParameter("bucketSeconds", bucket.seconds.toDouble())
                .setParameter("zone", zone.id)
                .setParameter("probeIds", probeIds)
                .withSplit(split)
                .withStatuses()
                .resultList as List<Array<Any?>>

        return rows.map { row ->
            UptimeBucketRow(
                probeId = row[0] as UUID,
                localStart =
                    when (val start = row[1]) {
                        is LocalDateTime -> start
                        is Timestamp -> start.toLocalDateTime()
                        else -> error("Unexpected bucket type ${start?.javaClass}")
                    },
                total = (row[2] as Number).toLong(),
                successes = (row[3] as Number).toLong(),
                failures = (row[4] as Number).toLong(),
                warnings = (row[5] as Number).toLong(),
                maintenance = (row[6] as Number).toLong(),
            )
        }
    }

    private fun Query.withSplit(split: RollupSplit): Query =
        setParameter("from", split.from)
            .setParameter("to", split.to)
            .setParameter("rollupStart", split.rollupStart)
            .setParameter("rollupEnd", split.rollupEnd)

    private fun Query.withStatuses(): Query =
        setParameter("success", ProbeMonitorLogStatus.SUCCESS.ordinal)
            .setParameter("warning", ProbeMonitorLogStatus.WARNING.ordinal)
            .setParameter("failure", ProbeMonitorLogStatus.FAILURE.ordinal)

    private fun toInstant(value: Any): Instant =
        when (value) {
            is Instant -> value
            is OffsetDateTime -> value.toInstant()
            is ZonedDateTime -> value.toInstant()
            is Timestamp -> value.toInstant()
            else -> error("Unexpected timestamp type ${value.javaClass}")
        }
}
