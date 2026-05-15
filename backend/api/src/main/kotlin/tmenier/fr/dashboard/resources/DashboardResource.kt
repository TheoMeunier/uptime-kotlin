package tmenier.fr.dashboard.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.repositories.DashboardRepository

@Path("/api/dashboard/stats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class DashboardResource(
    private val dashboardRepository: DashboardRepository,
) {
    @GET
    @Authenticated
    fun stats(): Response {
        val summary = dashboardRepository.getMonitorsSummary()
        val metricsLastDay = dashboardRepository.get24hResponseMetrics()
        val downProbes = dashboardRepository.findDownProbesWithDowntime()
        val getLatencySparkline = dashboardRepository.getLatencySparkline()
        val getIncidentBar = dashboardRepository.getIncidentBars()
        val getCheckSparkLine = dashboardRepository.getChecksSparkline()

        val result =
            mapOf(
                "summary" to summary,
                "metrics_last_days" to metricsLastDay,
                "down_probes" to downProbes,
                "latency_spark_line" to getLatencySparkline,
                "incident_bar" to getIncidentBar,
                "check_spark_line" to getCheckSparkLine,
            )

        return Response.ok(result).build()
    }
}
