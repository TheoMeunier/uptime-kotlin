package tmenier.fr.statuspages.resources

import io.quarkus.security.identity.SecurityIdentity
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Request
import jakarta.ws.rs.core.Response
import tmenier.fr.monitors.services.StatusSnapshotService.Companion.CACHE_POLICY
import tmenier.fr.monitors.services.StatusSnapshotService.Companion.PRIVATE_CACHE_POLICY
import tmenier.fr.statuspages.actions.PublicStatusFeedAction
import tmenier.fr.statuspages.services.PublicJsonResponder
import tmenier.fr.statuspages.services.StatusPageAccessGuard

@Path("/api/status-feed")
@Produces(MediaType.APPLICATION_JSON)
class GlobalStatusFeedResource(
    private val publicStatusFeedAction: PublicStatusFeedAction,
    private val responder: PublicJsonResponder,
    private val accessGuard: StatusPageAccessGuard,
    private val identity: SecurityIdentity,
) {
    @GET
    fun feed(
        @Context request: Request,
    ): Response {
        accessGuard.ensureGlobalReadable()

        val cachePolicy = if (identity.isAnonymous) CACHE_POLICY else PRIVATE_CACHE_POLICY
        return responder.respond(request, publicStatusFeedAction.forGlobal(), cachePolicy)
    }
}
