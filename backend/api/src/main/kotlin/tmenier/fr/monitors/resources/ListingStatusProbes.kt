package tmenier.fr.monitors.resources

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.repositories.ProbeRepository

@Path("/api/probes/status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListingStatusProbes(
    private val probeRepository: ProbeRepository,
) {
    @GET
    fun list(): Response {
        val probes = probeRepository.getProbesLastHour()

        return Response.ok(probes).build()
    }
}
