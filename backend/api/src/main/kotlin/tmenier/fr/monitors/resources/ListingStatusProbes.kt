package tmenier.fr.monitors.resources

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.repositories.MaintenanceOccurrenceRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.time.Duration
import java.time.Instant

@Path("/api/probes/status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListingStatusProbes(
    private val probeRepository: ProbeRepository,
    private val maintenanceOccurrenceRepository: MaintenanceOccurrenceRepository,
) {
    @GET
    fun list(): Response {
        val now = Instant.now()

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

        return Response.ok(probeRepository.getProbesLastHourWithMetrics(metrics)).build()
    }

    companion object {
        private const val MAINTENANCE_LOOKAHEAD_DAYS = 30L

        private const val UPTIME_WINDOW_DAYS = 30L
    }
}
