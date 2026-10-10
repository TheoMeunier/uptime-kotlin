package tmenier.fr.monitors.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.dtos.ProbeStatusDTO
import tmenier.fr.databases.repositories.MaintenanceOccurrenceRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID

@ApplicationScoped
class StatusSnapshotService(
    private val probeRepository: ProbeRepository,
    private val maintenanceOccurrenceRepository: MaintenanceOccurrenceRepository,
) {
    fun snapshot(
        now: Instant,
        probeIds: Collection<UUID>? = null,
    ): List<ProbeStatusDTO> {
        val maintenance =
            maintenanceOccurrenceRepository.findMaintenanceStateByProbe(
                at = now,
                horizon = now.plus(Duration.ofDays(MAINTENANCE_LOOKAHEAD_DAYS)),
                publicOnly = true,
            )
        val plannedDowntime =
            maintenanceOccurrenceRepository.sumMaintenanceSecondsByProbe(
                from = now.minus(Duration.ofDays(UPTIME_WINDOW_DAYS)),
                to = now,
            )

        val metrics =
            probeRepository.getProbesStatusMetrics(probeIds).mapValues { (probeId, probeMetrics) ->
                probeMetrics.copy(
                    maintenance = maintenance[probeId],
                    maintenanceSeconds = plannedDowntime[probeId] ?: 0L,
                )
            }

        return probeRepository.getProbesLastHourWithMetrics(metrics, probeIds)
    }

    fun fingerprint(
        now: Instant,
        vararg extra: Any?,
    ): String {
        val probes = probeRepository.getStatusFingerprint(since = now.minus(LAST_HOUR, ChronoUnit.HOURS))
        val maintenance =
            maintenanceOccurrenceRepository.getStatusFingerprint(
                from = now.minus(Duration.ofDays(UPTIME_WINDOW_DAYS)),
                at = now,
                horizon = now.plus(Duration.ofDays(MAINTENANCE_LOOKAHEAD_DAYS)),
            )

        val rolling = probes.failingProbes > 0L || maintenance.runningOccurrences > 0L
        val bucket = if (rolling) now.epochSecond / CACHE_MAX_AGE_SECONDS else 0L

        val seed =
            (
                listOf(
                    probes.enabledProbes,
                    probes.failingProbes,
                    probes.lastProbeUpdateAt,
                    probes.logsInWindow,
                    probes.lastLogAt,
                    maintenance.occurrences,
                    maintenance.visibleOccurrences,
                    maintenance.runningOccurrences,
                    maintenance.lastWindowUpdateAt,
                    bucket,
                ) + extra
            ).joinToString("|")

        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(MessageDigest.getInstance("SHA-256").digest(seed.toByteArray()))
            .take(ETAG_LENGTH)
    }

    companion object {
        private const val MAINTENANCE_LOOKAHEAD_DAYS = 30L

        private const val UPTIME_WINDOW_DAYS = 30L

        private const val LAST_HOUR = 1L

        const val CACHE_MAX_AGE_SECONDS = 30L

        const val CACHE_POLICY = "public, max-age=$CACHE_MAX_AGE_SECONDS, must-revalidate"

        private const val ETAG_LENGTH = 22
    }
}
