package tmenier.fr.twofactor.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.auth.extensions.userId
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.utils.noStore
import tmenier.fr.twofactor.actions.EnableTwoFactorAction
import tmenier.fr.twofactor.dtos.requests.EnableTwoFactorRequest

@Path("/api/profile/two-factor/enable")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class EnableTwoFactorResource(
    private val enableTwoFactorAction: EnableTwoFactorAction,
    private val jwt: JsonWebToken,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    @Transactional
    fun enable(
        @Valid payload: EnableTwoFactorRequest,
    ): Response = Response.ok(enableTwoFactorAction.execute(jwt.userId(), payload)).noStore().build()
}
