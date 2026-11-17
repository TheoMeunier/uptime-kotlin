package tmenier.fr.incidents.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.incidents.actions.ListIncidentsAction
import tmenier.fr.incidents.services.IncidentCsvWriter

@Path("/api/incidents/export")
class ExportIncidentsResource(
    private val listIncidentsAction: ListIncidentsAction,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    @Produces("text/csv")
    fun export(
        @QueryParam("probe_id") probeId: String?,
        @QueryParam("from") from: String?,
        @QueryParam("to") to: String?,
        @QueryParam("tz") timeZone: String?,
    ): Response {
        val export = listIncidentsAction.export(probeId, from, to, timeZone)

        return Response
            .ok(IncidentCsvWriter.write(export).toByteArray(Charsets.UTF_8))
            .type("text/csv; charset=UTF-8")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${IncidentCsvWriter.fileName(export)}\"")
            .build()
    }
}
