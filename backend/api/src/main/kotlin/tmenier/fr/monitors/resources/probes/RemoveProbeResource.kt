package tmenier.fr.monitors.resources.probes

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.monitors.entities.ProbesEntity
import java.util.UUID

@Path("/api/probes/{probeId}/remove")
@Produces(MediaType.APPLICATION_JSON)
class RemoveProbeResource {
    @POST
    @Authenticated
    @Transactional
    fun removeProbe(
        @PathParam("probeId") probeId: String,
    ): Response {
        ProbesEntity.delete(UUID.fromString(probeId))
        return Response.noContent().build()
    }
}
