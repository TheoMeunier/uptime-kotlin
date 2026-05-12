package tmenier.fr.monitors.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeRepository

@Path("/api/probes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListingProbesResource(
    private val probeRepository: ProbeRepository,
) {

    @GET
    @Authenticated
    fun list(): Response {
        val probes = probeRepository.getAll().map { ProbeMapper.toProbeListDto(it) }

        return Response.ok(probes).build()
    }
}
