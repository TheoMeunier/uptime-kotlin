package tmenier.fr.statuspages.resources

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.EntityTag
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Request
import jakarta.ws.rs.core.Response
import tmenier.fr.monitors.services.StatusSnapshotService
import tmenier.fr.monitors.services.StatusSnapshotService.Companion.CACHE_POLICY
import tmenier.fr.statuspages.actions.ListStatusPageAction
import tmenier.fr.statuspages.services.PublicStatusPageCache
import java.time.Instant

@Path("/api/status/{slug}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class PublicStatusPageResource(
    private val statusSnapshotService: StatusSnapshotService,
    private val listStatusPageAction: ListStatusPageAction,
    private val publicStatusPageCache: PublicStatusPageCache,
) {
    @GET
    fun show(
        @PathParam("slug") slug: String,
        @Context request: Request,
    ): Response {
        val now = Instant.now()
        val tag = EntityTag(statusSnapshotService.fingerprint(now, slug, listStatusPageAction.version(slug)), true)

        request.evaluatePreconditions(tag)?.let { notModified ->
            return notModified
                .tag(tag)
                .header(HttpHeaders.CACHE_CONTROL, CACHE_POLICY)
                .build()
        }

        return Response
            .ok(
                publicStatusPageCache.getOrCompute(slug, tag.value, now) {
                    listStatusPageAction.arrange(slug) { probeIds -> statusSnapshotService.snapshot(now, probeIds) }
                },
            ).tag(tag)
            .header(HttpHeaders.CACHE_CONTROL, CACHE_POLICY)
            .build()
    }
}
