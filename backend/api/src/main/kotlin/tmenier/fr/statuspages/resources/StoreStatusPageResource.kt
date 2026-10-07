package tmenier.fr.statuspages.resources

import io.quarkus.security.Authenticated
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.statuspages.actions.StoreStatusPageAction
import tmenier.fr.statuspages.requests.StoreStatusPageRequest

@Path("/api/status-pages/new")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class StoreStatusPageResource(
    private val storeStatusPageAction: StoreStatusPageAction,
) {
    @POST
    @Authenticated
    fun store(
        @Valid payload: StoreStatusPageRequest,
    ): Response = Response.ok(mapOf("id" to storeStatusPageAction.execute(payload))).build()
}
