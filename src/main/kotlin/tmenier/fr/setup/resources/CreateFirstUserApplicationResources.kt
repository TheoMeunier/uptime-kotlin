package tmenier.fr.setup.resources

import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.setup.actions.CreateFirstUserAction
import tmenier.fr.setup.dto.CreateFirstUserRequest

@Path("/api/app/setup/first-user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CreateFirstUserApplicationResources(
    private val createFirstUserApplicationAction: CreateFirstUserAction,
) {
    @POST
    fun store(
        @Valid payload: CreateFirstUserRequest,
    ): Response {
        val result = createFirstUserApplicationAction.execute(payload)
        return Response.ok(result).build()
    }
}
