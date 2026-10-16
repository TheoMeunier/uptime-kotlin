package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.Tuple
import tmenier.fr.common.enums.incidents.IncidentEventType
import tmenier.fr.databases.dtos.IncidentBar
import tmenier.fr.databases.dtos.IncidentEventDto
import tmenier.fr.databases.dtos.MonitorSummary
import tmenier.fr.databases.dtos.ResponseMetrics24h
import tmenier.fr.databases.dtos.SparklinePoint
import tmenier.fr.databases.entities.IncidentEntity
import java.sql.Timestamp
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

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
        val since = Instant.now().minus(24, ChronoUnit.HOURS)

        val jpql =
            """
            SELECT
              COALESCE(AVG(pml.responseTime), 0),
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
            countChecks24h = ((single[1] as Number?) ?: 0L).toLong(),
        )
    }

    fun getLatencySparkline(): List<SparklinePoint> {
        val since = Instant.now().minus(24, ChronoUnit.HOURS)

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
                bucket = toInstant(row[0], "bucket"),
                value = (row[1] as Number).toDouble(),
            )
        }
    }

    fun getIncidentBars(): List<IncidentBar> {
        val since = Instant.now().minus(24, ChronoUnit.HOURS)

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
                hour = toInstant(row[0], "hour"),
                upCount = (row[1] as Number).toLong(),
                downCount = (row[2] as Number).toLong(),
            )
        }
    }

    fun getChecksSparkline(): List<SparklinePoint> {
        val since = Instant.now().minus(24, ChronoUnit.HOURS)

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
                bucket = toInstant(row[0], "bucket"),
                value = (row[1] as Number).toDouble(),
            )
        }
    }

    /**
     * The latest openings and closings of incidents, newest first, all probes included: a probe
     * paused during an outage shows how its incident ended.
     *
     * Reading the [limit] incidents whose last event is the most recent is enough. An incident
     * ranked lower has all its events older than the last event of each incident above it, so
     * it cannot place one among the [limit] newest. The table holds a few dozen rows per probe
     * and per year, and the retention purge never touches it.
     */
    fun getRecentEvents(limit: Int = 15): List<IncidentEventDto> =
        em
            .createQuery(
                """
                SELECT i FROM IncidentEntity i JOIN FETCH i.probe
                ORDER BY COALESCE(i.endedAt, i.startedAt) DESC, i.id
                """.trimIndent(),
                IncidentEntity::class.java,
            ).setMaxResults(limit)
            .resultList
            .flatMap(::eventsOf)
            .sortedWith(compareByDescending<IncidentEventDto> { it.at }.thenByDescending { it.type })
            .take(limit)

    private fun eventsOf(incident: IncidentEntity): List<IncidentEventDto> {
        val opened =
            IncidentEventDto(
                incidentId = incident.id,
                probeId = incident.probe.id,
                probeName = incident.probe.name,
                type = IncidentEventType.OPENED,
                at = incident.startedAt,
                message = incident.openingMessage,
                ongoing = incident.endedAt == null,
                durationSeconds = null,
                closedReason = null,
            )
        val endedAt = incident.endedAt ?: return listOf(opened)

        val closed =
            opened.copy(
                type = IncidentEventType.CLOSED,
                at = endedAt,
                durationSeconds = incident.durationSeconds,
                closedReason = incident.closedReason,
            )
        return listOf(closed, opened)
    }

    private fun toInstant(
        value: Any?,
        column: String,
    ): Instant =
        when (value) {
            is Instant -> value
            is OffsetDateTime -> value.toInstant()
            is Timestamp -> value.toInstant()
            else -> error("Unsupported timestamp type for column '$column': ${value?.javaClass?.name}")
        }
}
