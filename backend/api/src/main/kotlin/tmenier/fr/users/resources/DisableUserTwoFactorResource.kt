package tmenier.fr.users.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.auth.extensions.userId
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.users.actions.DisableUserTwoFactorAction
import java.util.UUID

@Path("/api/users/{userId}/disable-two-factor")
@Produces(MediaType.APPLICATION_JSON)
class DisableUserTwoFactorResource(
    private val disableUserTwoFactorAction: DisableUserTwoFactorAction,
    private val jwt: JsonWebToken,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun disable(
        @PathParam("userId") userId: UUID,
    ): Response {
        disableUserTwoFactorAction.execute(jwt.userId(), userId)
        return Response.noContent().build()
    }
}
