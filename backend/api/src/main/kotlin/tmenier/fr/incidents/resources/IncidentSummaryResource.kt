package tmenier.fr.incidents.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.incidents.actions.ListIncidentsAction

@Path("/api/incidents/summary")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class IncidentSummaryResource(
    private val listIncidentsAction: ListIncidentsAction,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun summary(
        @QueryParam("probe_id") probeId: String?,
        @QueryParam("from") from: String?,
        @QueryParam("to") to: String?,
    ): Response = Response.ok(listIncidentsAction.summary(probeId, from, to)).build()
}
