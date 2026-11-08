package tmenier.fr.monitors.resources

import io.quarkus.security.identity.SecurityIdentity
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.EntityTag
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Request
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.repositories.StatusPageRepository
import tmenier.fr.monitors.services.StatusSnapshotService
import tmenier.fr.monitors.services.StatusSnapshotService.Companion.CACHE_POLICY
import tmenier.fr.monitors.services.StatusSnapshotService.Companion.PRIVATE_CACHE_POLICY
import tmenier.fr.statuspages.services.StatusPageAccessGuard
import java.time.Instant

@Path("/api/probes/status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListingStatusProbes(
    private val statusSnapshotService: StatusSnapshotService,
    private val statusPageRepository: StatusPageRepository,
    private val accessGuard: StatusPageAccessGuard,
    private val identity: SecurityIdentity,
) {
    @GET
    fun list(
        @Context request: Request,
    ): Response {
        accessGuard.ensureGlobalReadable()

        val now = Instant.now()
        val anonymous = identity.isAnonymous
        val hidden = if (anonymous) statusPageRepository.findPrivateOnlyProbeIds() else emptySet()
        val cachePolicy = if (anonymous) CACHE_POLICY else PRIVATE_CACHE_POLICY
        val tag =
            EntityTag(
                statusSnapshotService.fingerprint(now, if (anonymous) "anonymous" else "signed-in", hidden.sorted().joinToString(",")),
                true,
            )

        request.evaluatePreconditions(tag)?.let { notModified ->
            return notModified
                .tag(tag)
                .header(HttpHeaders.CACHE_CONTROL, cachePolicy)
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .build()
        }

        val probes = statusSnapshotService.snapshot(now).filterNot { it.probe.id in hidden }

        return Response
            .ok(probes)
            .tag(tag)
            .header(HttpHeaders.CACHE_CONTROL, cachePolicy)
            .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
            .build()
    }
}
