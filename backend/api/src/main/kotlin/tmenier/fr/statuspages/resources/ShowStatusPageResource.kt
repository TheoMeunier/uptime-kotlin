package tmenier.fr.statuspages.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.statuspages.actions.ListStatusPageAction
import java.util.UUID

@Path("/api/status-pages/{statusPageId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ShowStatusPageResource(
    private val listStatusPageAction: ListStatusPageAction,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun show(
        @PathParam("statusPageId") statusPageId: UUID,
    ): Response = Response.ok(listStatusPageAction.show(statusPageId)).build()
}
