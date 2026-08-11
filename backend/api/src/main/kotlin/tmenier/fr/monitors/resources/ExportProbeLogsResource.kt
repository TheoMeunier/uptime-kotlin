package tmenier.fr.monitors.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.entities.ProbesMonitorsLogEntity
import tmenier.fr.databases.repositories.ProbeMonitorRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.util.UUID

@Path("/api/probes/{probeId}/logs/export")
class ExportProbeLogsResource(
    private val probeRepository: ProbeRepository,
    private val probeMonitorRepository: ProbeMonitorRepository,
) {
    @GET
    @Authenticated
    @Produces("text/csv")
    fun exportProbeLogs(
        @PathParam("probeId") probeId: String,
    ): Response {
        val uuid = UUID.fromString(probeId)

        if (probeRepository.findByIdOrNull(uuid) == null) {
            throw NotFoundException("Probe not found")
        }

        val csv = buildCsv(probeMonitorRepository.findByProbe(uuid))

        return Response
            .ok(csv)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"monitor-logs-$uuid.csv\"")
            .build()
    }

    private fun buildCsv(logs: List<ProbesMonitorsLogEntity>): String {
        val rows =
            logs.map { log ->
                listOf(
                    log.id.toString(),
                    log.status.name,
                    log.responseTime.toString(),
                    log.message,
                    log.runAt.toString(),
                )
            }

        return (listOf(listOf("id", "status", "response_time_ms", "message", "run_at")) + rows)
            .joinToString("\n") { row -> row.joinToString(",") { escapeCsvValue(it) } }
    }

    private fun escapeCsvValue(value: String): String {
        if (value.none { it == '"' || it == ',' || it == '\n' || it == '\r' }) {
            return value
        }

        return "\"${value.replace("\"", "\"\"")}\""
    }
}
