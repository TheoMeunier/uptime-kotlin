package tmenier.fr.http.auth.resources

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response
import tmenier.fr.actions.auth.LoginAction
import tmenier.fr.actions.auth.RefreshTokenAction
import tmenier.fr.http.auth.dtos.requests.LoginRequest
import tmenier.fr.http.auth.dtos.requests.RefreshTokenRequest

@ApplicationScoped
class RefreshTokenResource(private val refreshTokenAction: RefreshTokenAction) {

    @POST
    @Path("/refresh")
    @Transactional
    fun login(
        @Valid payload: RefreshTokenRequest
    ): Response {
        val result = refreshTokenAction.execute(payload)
        return Response.ok(result).build()
    }
}