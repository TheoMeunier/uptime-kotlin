package tmenier.fr.monitors.resources

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.EntityTag
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Request
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.repositories.MaintenanceOccurrenceRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.Base64

@Path("/api/probes/status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListingStatusProbes(
    private val probeRepository: ProbeRepository,
    private val maintenanceOccurrenceRepository: MaintenanceOccurrenceRepository,
) {
    @GET
    fun list(
        @Context request: Request,
    ): Response {
        val now = Instant.now()
        val tag = EntityTag(fingerprint(now), true)

        request.evaluatePreconditions(tag)?.let { notModified ->
            return notModified
                .tag(tag)
                .header(HttpHeaders.CACHE_CONTROL, CACHE_POLICY)
                .build()
        }

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
            probeRepository.getProbesStatusMetrics().mapValues { (probeId, probeMetrics) ->
                probeMetrics.copy(
                    maintenance = maintenance[probeId],
                    maintenanceSeconds = plannedDowntime[probeId] ?: 0L,
                )
            }

        return Response
            .ok(probeRepository.getProbesLastHourWithMetrics(metrics))
            .tag(tag)
            .header(HttpHeaders.CACHE_CONTROL, CACHE_POLICY)
            .build()
    }

    private fun fingerprint(now: Instant): String {
        val probes = probeRepository.getStatusFingerprint(since = LocalDateTime.now().minusHours(LAST_HOUR))
        val maintenance =
            maintenanceOccurrenceRepository.getStatusFingerprint(
                from = now.minus(Duration.ofDays(UPTIME_WINDOW_DAYS)),
                at = now,
                horizon = now.plus(Duration.ofDays(MAINTENANCE_LOOKAHEAD_DAYS)),
            )

        val rolling = probes.failingProbes > 0L || maintenance.runningOccurrences > 0L
        val bucket = if (rolling) now.epochSecond / CACHE_MAX_AGE_SECONDS else 0L

        val seed =
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

        private const val CACHE_MAX_AGE_SECONDS = 30L

        private const val CACHE_POLICY = "public, max-age=$CACHE_MAX_AGE_SECONDS, must-revalidate"

        private const val ETAG_LENGTH = 22
    }
}
