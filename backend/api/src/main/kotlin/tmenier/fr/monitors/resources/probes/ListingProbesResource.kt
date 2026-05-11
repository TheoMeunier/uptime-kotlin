package tmenier.fr.monitors.resources.probes

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.ProbeContentMapper
import tmenier.fr.databases.mappers.toUrl
import tmenier.fr.monitors.dtos.responses.ProbeListDTO

@Path("/api/probes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListingProbesResource {
    @GET
    @Authenticated
    fun list(): Response {
        val probes =
            ProbesEntity.getAllProbes().map {
                ProbeListDTO(
                    it.id,
                    it.name,
                    it.description,
                    ProbeContentMapper.toDto(it).toUrl(),
                    it.status,
                )
            }

        return Response.ok(probes).build()
    }
}
