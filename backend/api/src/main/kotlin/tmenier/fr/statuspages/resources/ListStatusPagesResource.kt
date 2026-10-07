package tmenier.fr.statuspages.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.statuspages.actions.ListStatusPageAction

@Path("/api/status-pages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListStatusPagesResource(
    private val listStatusPageAction: ListStatusPageAction,
) {
    @GET
    @Authenticated
    fun list(): Response = Response.ok(listStatusPageAction.execute()).build()
}
