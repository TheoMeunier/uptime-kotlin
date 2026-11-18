package tmenier.fr.statuspages.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.statuspages.actions.StoreStatusPageAction
import tmenier.fr.statuspages.domains.RequestHost
import tmenier.fr.statuspages.requests.StoreStatusPageRequest
import java.util.UUID

@Path("/api/status-pages/{statusPageId}/update")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateStatusPageResource(
    private val storeStatusPageAction: StoreStatusPageAction,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun update(
        @PathParam("statusPageId") statusPageId: UUID,
        @Valid payload: StoreStatusPageRequest,
        @Context headers: HttpHeaders,
    ): Response {
        val id = storeStatusPageAction.execute(payload, statusPageId, RequestHost.resolve(headers::getHeaderString))
        return Response.ok(mapOf("id" to id)).build()
    }
}
