package tmenier.fr.auth.resources

import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.auth.actions.LogoutAction
import tmenier.fr.auth.dtos.requests.RefreshTokenRequest

/**
 * Left unauthenticated on purpose: logging out must still work when the access token has already
 * expired. Holding the refresh token is the proof of ownership here, and the endpoint only ever
 * deletes that exact token.
 */
@Path("/api/auth/logout")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class LogoutResource(
    private val logoutAction: LogoutAction,
) {
    @POST
    @Transactional
    fun logout(
        @Valid payload: RefreshTokenRequest,
    ): Response {
        logoutAction.execute(payload)
        return Response.noContent().build()
    }
}
