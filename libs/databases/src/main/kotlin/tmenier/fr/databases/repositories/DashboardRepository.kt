package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.Tuple
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.utils.toHumanReadable
import tmenier.fr.databases.dtos.DownProbeDto
import tmenier.fr.databases.dtos.IncidentBar
import tmenier.fr.databases.dtos.MonitorSummary
import tmenier.fr.databases.dtos.ProbeEventDto
import tmenier.fr.databases.dtos.ResponseMetrics24h
import tmenier.fr.databases.dtos.SparklinePoint
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

@ApplicationScoped
class DashboardRepository(
    private val em: EntityManager,
) {
    fun getMonitorsSummary(): MonitorSummary {
        val jpql =
            """
            SELECT
              COUNT(p),
              SUM(CASE WHEN p.status = 0 THEN 1 ELSE 0 END),
              SUM(CASE WHEN p.status = 3 THEN 1 ELSE 0 END),
              COALESCE(ROUND(AVG(CASE WHEN p.status = 0 THEN 1.0 ELSE 0.0 END) * 100, 2), 0)
            FROM ProbesEntity p
            WHERE p.enabled = true
            """.trimIndent()

        val single = em.createQuery(jpql).singleResult as Array<*>

        return MonitorSummary(
            totalMonitors = (single[0] as Number).toLong(),
            totalMonitorsSuccess = ((single[1] as Number?) ?: 0L).toLong(),
            totalMonitorsFailures = ((single[2] as Number?) ?: 0L).toLong(),
            avgUptimePercent = ((single[3] as Number?) ?: 0.0).toDouble(),
        )
    }

    fun get24hResponseMetrics(): ResponseMetrics24h {
        val since: LocalDateTime =
            LocalDateTime.now(ZoneOffset.UTC).minus(24, ChronoUnit.HOURS)

        val jpql =
            """
            SELECT
              COALESCE(AVG(pml.responseTime), 0),
              SUM(CASE WHEN pml.status = 3 THEN 1 ELSE 0 END),
              COUNT(pml)
            FROM ProbesEntity p
            LEFT JOIN p.probesMonitorLogs pml WITH pml.runAt > :since
            WHERE p.enabled = true
            """.trimIndent()

        val single =
            em
                .createQuery(jpql)
                .setParameter("since", since)
                .singleResult as Array<*>

        return ResponseMetrics24h(
            avgResponseTimeMs = ((single[0] as Number?) ?: 0.0).toDouble(),
            countIncidents24h = ((single[1] as Number?) ?: 0L).toLong(),
            countChecks24h = ((single[2] as Number?) ?: 0L).toLong(),
        )
    }

    fun findDownProbesWithDowntime(): List<DownProbeDto> {
        val jpql =
            """
            SELECT p.id, p.name, MAX(pml.runAt), p.createdAt
            FROM ProbesEntity p
            LEFT JOIN p.probesMonitorLogs pml WITH pml.status = 0
            WHERE p.enabled = true AND p.status = 3
            GROUP BY p.id, p.name, p.createdAt
            """.trimIndent()

        val results = em.createQuery(jpql, Tuple::class.java).resultList

        val now = LocalDateTime.now(ZoneOffset.UTC)

        return results.map { row ->
            val id = (row[0] as UUID)
            val name = row[1] as String
            val lastSuccess = row[2] as LocalDateTime?
            val createdAt = row[3] as LocalDateTime

            val since = lastSuccess ?: createdAt
            val duration = Duration.between(since, now)

            DownProbeDto(
                id = id,
                name = name,
                downDuration = duration.toHumanReadable(),
            )
        }
    }

    fun getLatencySparkline(): List<SparklinePoint> {
        val since = LocalDateTime.now(ZoneOffset.UTC).minus(24, ChronoUnit.HOURS)

        val jpql =
            """
            SELECT
                function('date_trunc', 'hour', pml.runAt),
                COALESCE(AVG(pml.responseTime), 0)
            FROM ProbesMonitorsLogEntity pml
            JOIN pml.probe p
            WHERE pml.runAt > :since AND p.enabled = true
            GROUP BY function('date_trunc', 'hour', pml.runAt)
            ORDER BY function('date_trunc', 'hour', pml.runAt)
            """.trimIndent()

        val results =
            em
                .createQuery(jpql, Tuple::class.java)
                .setParameter("since", since)
                .resultList

        return results.map { row ->
            SparklinePoint(
                bucket = row[0] as LocalDateTime,
                value = (row[1] as Number).toDouble(),
            )
        }
    }

    fun getIncidentBars(): List<IncidentBar> {
        val since = LocalDateTime.now(ZoneOffset.UTC).minus(24, ChronoUnit.HOURS)

        val jpql =
            """
             SELECT
                function('date_trunc', 'hour', pml.runAt),
                SUM(CASE WHEN pml.status = 0 THEN 1 ELSE 0 END),
                SUM(CASE WHEN pml.status = 3 THEN 1 ELSE 0 END)
            FROM ProbesMonitorsLogEntity pml
            JOIN pml.probe p
            WHERE pml.runAt > :since AND p.enabled = true
            GROUP BY function('date_trunc', 'hour', pml.runAt)
            ORDER BY function('date_trunc', 'hour', pml.runAt)
            """.trimIndent()

        val results =
            em
                .createQuery(jpql, Tuple::class.java)
                .setParameter("since", since)
                .resultList

        return results.map { row ->
            IncidentBar(
                hour = row[0] as LocalDateTime,
                upCount = (row[1] as Number).toLong(),
                downCount = (row[2] as Number).toLong(),
            )
        }
    }

    fun getChecksSparkline(): List<SparklinePoint> {
        val since = LocalDateTime.now(ZoneOffset.UTC).minus(24, ChronoUnit.HOURS)

        val jpql =
            """
            SELECT
                function('date_trunc', 'hour', pml.runAt),
                COUNT(pml)
            FROM ProbesMonitorsLogEntity pml
            JOIN pml.probe p
            WHERE pml.runAt > :since AND p.enabled = true
            GROUP BY function('date_trunc', 'hour', pml.runAt)
            ORDER BY function('date_trunc', 'hour', pml.runAt)
            """.trimIndent()

        val results =
            em
                .createQuery(jpql, Tuple::class.java)
                .setParameter("since", since)
                .resultList

        return results.map { row ->
            SparklinePoint(
                bucket = row[0] as LocalDateTime,
                value = (row[1] as Number).toDouble(),
            )
        }
    }


    fun getRecentEvents(limit: Int = 15): List<ProbeEventDto> {
        val since = LocalDateTime.now(ZoneOffset.UTC).minus(7, ChronoUnit.DAYS)

        val sql =
            """
            SELECT probe_id, probe_name, status, message, run_at
            FROM (
                SELECT pml.probe_id       AS probe_id,
                       p.name             AS probe_name,
                       pml.status         AS status,
                       pml.message        AS message,
                       pml.run_at         AS run_at,
                       LAG(pml.status) OVER (PARTITION BY pml.probe_id ORDER BY pml.run_at) AS previous_status
                FROM probes_monitors_logs pml
                JOIN probes p ON p.id = pml.probe_id
                WHERE pml.run_at > :since AND p.enabled = true
            ) transitions
            WHERE status IS DISTINCT FROM previous_status
            ORDER BY run_at DESC
            """.trimIndent()

        val rows =
            em
                .createNativeQuery(sql, Tuple::class.java)
                .setParameter("since", since)
                .setMaxResults(limit)
                .resultList as List<Tuple>

        val statuses = ProbeMonitorLogStatus.entries

        return rows.map { row ->
            val ordinal = (row.get("status") as Number).toInt()

            ProbeEventDto(
                probeId = row.get("probe_id") as UUID,
                probeName = row.get("probe_name") as String,
                status = statuses.getOrNull(ordinal)?.name ?: ProbeMonitorLogStatus.FAILURE.name,
                message = row.get("message") as? String ?: "",
                runAt = row.readTimestamp("run_at"),
            )
        }
    }

    /**
     * A native query hands back whatever the JDBC driver produced for a timestamp column, and that
     * varies: the PostgreSQL driver returns a LocalDateTime here, others still return a
     * java.sql.Timestamp. Accept both rather than betting on one.
     */
    private fun Tuple.readTimestamp(column: String): LocalDateTime =
        when (val value = this.get(column)) {
            is LocalDateTime -> value
            is Timestamp -> value.toLocalDateTime()
            is OffsetDateTime -> value.toLocalDateTime()
            is Instant -> LocalDateTime.ofInstant(value, ZoneOffset.UTC)
            else -> error("Unsupported timestamp type for column '$column': ${value?.javaClass?.name}")
        }
}
