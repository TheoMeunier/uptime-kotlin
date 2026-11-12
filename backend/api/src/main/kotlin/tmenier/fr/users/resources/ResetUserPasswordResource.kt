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
import tmenier.fr.users.actions.ResetUserPasswordAction
import java.util.UUID

@Path("/api/users/{userId}/reset-password")
@Produces(MediaType.APPLICATION_JSON)
class ResetUserPasswordResource(
    private val resetUserPasswordAction: ResetUserPasswordAction,
    private val jwt: JsonWebToken,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun reset(
        @PathParam("userId") userId: UUID,
    ): Response {
        val password = resetUserPasswordAction.execute(UUID.fromString(jwt.name), userId)

        return Response
            .ok(mapOf("password" to password))
            .header("Cache-Control", "no-store")
            .build()
    }
}
