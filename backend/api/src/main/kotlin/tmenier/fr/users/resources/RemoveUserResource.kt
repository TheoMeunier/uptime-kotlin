package tmenier.fr.users.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.users.actions.RemoveUserAction
import java.util.UUID

@Path("/api/users/{userId}/remove")
@Produces(MediaType.APPLICATION_JSON)
class RemoveUserResource(
    private val removeUserAction: RemoveUserAction,
    private val jwt: JsonWebToken,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun remove(
        @PathParam("userId") userId: UUID,
    ): Response {
        removeUserAction.execute(UUID.fromString(jwt.name), userId)

        return Response.noContent().build()
    }
}
