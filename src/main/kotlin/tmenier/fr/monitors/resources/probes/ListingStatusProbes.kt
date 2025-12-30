package tmenier.fr.monitors.resources.probes

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.entities.mapper.toStatusDto

@Path("/api/probes/status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListingStatusProbes {
    @GET
    @Authenticated
    fun list(): Response {
        val probes = ProbesEntity.getProbesLastHour().map {
            it.toStatusDto()
        }

        return Response.ok(probes).build()
    }
}
