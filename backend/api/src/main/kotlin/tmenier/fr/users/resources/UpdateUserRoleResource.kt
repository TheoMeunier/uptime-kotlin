package tmenier.fr.users.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.users.actions.UpdateUserRoleAction
import tmenier.fr.users.requests.UpdateUserRoleRequest
import java.util.UUID

@Path("/api/users/{userId}/role")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateUserRoleResource(
    private val updateUserRoleAction: UpdateUserRoleAction,
    private val jwt: JsonWebToken,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun update(
        @PathParam("userId") userId: UUID,
        @Valid payload: UpdateUserRoleRequest,
    ): Response {
        updateUserRoleAction.execute(UUID.fromString(jwt.name), userId, payload.role)

        return Response.noContent().build()
    }
}
