package tmenier.fr.monitors.resources.probes

import io.quarkus.security.Authenticated
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.entities.mapper.toProbeWithNotificationsDTO
import tmenier.fr.monitors.entities.mapper.toShowDtp
import java.util.UUID

@Path("/api/probes/{probeId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ShowProbeResource {
    @GET
    @Authenticated
    fun show(
        @PathParam("probeId") probeId: String,
        @QueryParam("hours") hours: Long,
    ): Response {
        if (hours == 0L) {
            val probeEntity =
                ProbesEntity.findById(UUID.fromString(probeId)) ?: throw NotFoundException("Probe not found")

            return Response.ok(probeEntity.toProbeWithNotificationsDTO()).build()
        }

        val validHours = setOf(1L, 3L, 6L, 24L, 168L)
        if (hours !in validHours) {
            throw BadRequestException("hours must be one of: 1, 3, 6, 24, 168")
        }

        val probeEntity =
            ProbesEntity.findByIdWithLogs(UUID.fromString(probeId), hours)
                ?: throw NotFoundException("Probe not found")

        return Response.ok(probeEntity.toShowDtp()).build()
    }
}
