package tmenier.fr.auth.resources

import jakarta.annotation.security.PermitAll
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.auth.actions.LoginTotpAction
import tmenier.fr.auth.dtos.requests.LoginTotpRequest
import tmenier.fr.common.exceptions.core.ApiException

@Path("/api/auth/login/totp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class LoginTotpResource(
    private val loginTotpAction: LoginTotpAction,
) {
    @POST
    @PermitAll
    @Transactional(dontRollbackOn = [ApiException::class])
    fun login(
        @Valid payload: LoginTotpRequest,
    ): Response = Response.ok(loginTotpAction.execute(payload)).build()
}
