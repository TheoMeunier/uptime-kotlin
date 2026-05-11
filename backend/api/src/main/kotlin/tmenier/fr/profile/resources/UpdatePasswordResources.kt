package tmenier.fr.profile.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.profile.actions.UpdatePasswordAction
import tmenier.fr.profile.dtos.requests.UpdatePasswordRequest

@Path("/api/profile/update/password")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdatePasswordResources(
    private val updatePasswordAction: UpdatePasswordAction,
) {
    @POST
    @Transactional
    @Authenticated
    fun updatePassword(
        @Valid payload: UpdatePasswordRequest,
    ): Response {
        updatePasswordAction.execute(payload)
        return Response.ok().build()
    }
}
