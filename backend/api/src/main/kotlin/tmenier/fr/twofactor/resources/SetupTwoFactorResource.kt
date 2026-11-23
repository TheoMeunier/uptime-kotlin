package tmenier.fr.twofactor.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.auth.extensions.userId
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.utils.noStore
import tmenier.fr.twofactor.actions.SetupTwoFactorAction

@Path("/api/profile/two-factor/setup")
@Produces(MediaType.APPLICATION_JSON)
class SetupTwoFactorResource(
    private val setupTwoFactorAction: SetupTwoFactorAction,
    private val jwt: JsonWebToken,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    @Transactional
    fun setup(): Response = Response.ok(setupTwoFactorAction.execute(jwt.userId())).noStore().build()
}
