package tmenier.fr.auth.resources

import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.auth.actions.LoginAction
import tmenier.fr.auth.dtos.requests.LoginRequest

@Path("/api/auth/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class LoginResource(private val loginAction: LoginAction) {

    @POST
    @Transactional
    fun login(
        @Valid payload: LoginRequest
    ): Response {
        val result = loginAction.execute(payload)
        return Response.ok(result).build()
    }
}