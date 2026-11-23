package tmenier.fr.auth.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.auth.actions.ListingSessionsAction
import tmenier.fr.common.config.DemoMode
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.utils.toUuidOrNull

@Path("/api/auth/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListingSessionsResource(
    private val listingSessionsAction: ListingSessionsAction,
    private val demoMode: DemoMode,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun sessions(
        @QueryParam("current") current: String?,
    ): Response {
        val sessions = listingSessionsAction.execute(current.toUuidOrNull())

        return Response.ok(if (demoMode.enabled) sessions.filter { it.current } else sessions).build()
    }
}
