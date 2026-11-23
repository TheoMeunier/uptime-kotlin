package tmenier.fr.twofactor.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.auth.extensions.userId
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.twofactor.actions.ShowTwoFactorAction

@Path("/api/profile/two-factor")
@Produces(MediaType.APPLICATION_JSON)
class ShowTwoFactorResource(
    private val showTwoFactorAction: ShowTwoFactorAction,
    private val jwt: JsonWebToken,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun show(): Response = Response.ok(showTwoFactorAction.execute(jwt.userId())).build()
}
