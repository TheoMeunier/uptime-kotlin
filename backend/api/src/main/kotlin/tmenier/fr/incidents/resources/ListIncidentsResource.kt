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

@Path("/api/incidents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListIncidentsResource(
    private val listIncidentsAction: ListIncidentsAction,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun list(
        @QueryParam("state") state: String?,
        @QueryParam("probe_id") probeId: String?,
        @QueryParam("from") from: String?,
        @QueryParam("to") to: String?,
        @QueryParam("page") page: Int?,
        @QueryParam("size") size: Int?,
    ): Response =
        Response
            .ok(
                listIncidentsAction.execute(
                    state = state,
                    probeId = probeId,
                    from = from,
                    to = to,
                    page = page ?: 0,
                    size = size ?: ListIncidentsAction.DEFAULT_PAGE_SIZE,
                ),
            ).build()
}
