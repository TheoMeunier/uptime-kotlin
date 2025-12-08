package tmenier.fr.http.auth.resources

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response
import tmenier.fr.actions.auth.LoginAction
import tmenier.fr.http.auth.dtos.requests.LoginRequest

@ApplicationScoped
class LoginResource(private val loginAction: LoginAction) {

    @POST
    @Path("/login")
    @Transactional
    fun login(
        @Valid payload: LoginRequest
    ): Response {
        val result = loginAction.execute(payload)
        return Response.ok(result).build()
    }
}