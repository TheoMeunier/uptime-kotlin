package tmenier.fr.monitors.resources.probes

import io.quarkus.security.Authenticated
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.entities.mapper.toShowDtp
import java.util.*

@Path("/api/probes/{probeId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ShowProbeResource {

    @GET
    @Authenticated
    fun show(
        @PathParam("probeId") probeId: String
    ): Response {
        val probeEntity = ProbesEntity.findById(UUID.fromString(probeId)) ?: throw NotFoundException("Probe not found")

        return Response.ok(probeEntity.toShowDtp()).build()
    }
}