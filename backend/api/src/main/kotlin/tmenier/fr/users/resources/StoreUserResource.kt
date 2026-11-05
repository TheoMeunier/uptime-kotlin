package tmenier.fr.users.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.users.actions.StoreUserAction
import tmenier.fr.users.requests.StoreUserRequest

@Path("/api/users/new")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class StoreUserResource(
    private val storeUserAction: StoreUserAction,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun store(
        @Valid payload: StoreUserRequest,
    ): Response = Response.ok(mapOf("id" to storeUserAction.execute(payload))).build()
}
