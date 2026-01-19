package tmenier.fr.dashboard.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.dashboard.services.StatDashboardService

@Path("/api/dashboard/stats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class DashboardResource(
    private val dashboardService: StatDashboardService,
) {
    @GET
    @Authenticated
    fun stats(): Response {
        val summary = dashboardService.getMonitorsSummary()
        val metricsLastDay = dashboardService.get24hResponseMetrics()
        val downProbes = dashboardService.findDownProbesWithDowntime()

        val result =
            mapOf(
                "summary" to summary,
                "metrics_last_days" to metricsLastDay,
                "down_probes" to downProbes,
            )

        return Response.ok(result).build()
    }
}
