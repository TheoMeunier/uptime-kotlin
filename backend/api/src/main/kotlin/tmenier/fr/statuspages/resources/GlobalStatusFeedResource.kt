package tmenier.fr.statuspages.resources

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Request
import jakarta.ws.rs.core.Response
import tmenier.fr.statuspages.actions.PublicStatusFeedAction
import tmenier.fr.statuspages.services.PublicJsonResponder

@Path("/api/status-feed")
@Produces(MediaType.APPLICATION_JSON)
class GlobalStatusFeedResource(
    private val publicStatusFeedAction: PublicStatusFeedAction,
    private val responder: PublicJsonResponder,
) {
    @GET
    fun feed(
        @Context request: Request,
    ): Response = responder.respond(request, publicStatusFeedAction.forGlobal())
}
