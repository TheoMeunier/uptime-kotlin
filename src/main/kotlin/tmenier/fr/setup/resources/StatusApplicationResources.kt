package tmenier.fr.setup.resources

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.setup.actions.GetStatusApplicationAction

@Path("/api/app/status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class StatusApplicationResources(
    private val getStatusApplicationAction: GetStatusApplicationAction
) {

    @GET
    fun getStatusApplication(): Response {
        val result = getStatusApplicationAction.execute()

        val status = mapOf(
            "status" to result
        )

        return Response.ok(status).build()
    }
}