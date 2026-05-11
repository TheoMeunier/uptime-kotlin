package tmenier.fr.auth.resources

import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.auth.actions.RefreshTokenAction
import tmenier.fr.auth.dtos.requests.RefreshTokenRequest

@Path("/api/auth/refresh")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RefreshTokenResource(
    private val refreshTokenAction: RefreshTokenAction,
) {
    @POST
    @Transactional
    fun login(
        @Valid payload: RefreshTokenRequest,
    ): Response {
        val result = refreshTokenAction.execute(payload)
        return Response.ok(result).build()
    }
}
