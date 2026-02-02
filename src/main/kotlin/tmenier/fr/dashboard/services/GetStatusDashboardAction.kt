package tmenier.fr.dashboard.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import tmenier.fr.dashboard.dtos.DownProbeDto
import tmenier.fr.dashboard.dtos.MonitorSummary
import tmenier.fr.dashboard.dtos.ResponseMetrics24h
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

@ApplicationScoped
class StatDashboardService(
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

        val results = em.createQuery(jpql).resultList as List<Array<*>>

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
}

fun Duration.toHumanReadable(): String {
    val totalSeconds = this.seconds

    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (days > 0) append("${days}j ")
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m ")
        if (days == 0L && seconds > 0) append("${seconds}s")
    }.trim()
}
