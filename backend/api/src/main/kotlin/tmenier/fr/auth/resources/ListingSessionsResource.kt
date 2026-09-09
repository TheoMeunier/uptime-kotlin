package tmenier.fr.auth.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.auth.actions.ListingSessionsAction
import java.util.UUID

@Path("/api/auth/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListingSessionsResource(
    private val listingSessionsAction: ListingSessionsAction,
) {
    @GET
    @Authenticated
    fun sessions(
        @QueryParam("current") current: String?,
    ): Response {
        val currentSessionId = current?.let { runCatching { UUID.fromString(it) }.getOrNull() }

        return Response.ok(listingSessionsAction.execute(currentSessionId)).build()
    }
}
