package tmenier.fr.monitors.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.repositories.ProbeMonitorRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.util.UUID

@Path("/api/probes/{probeId}/logs/purge")
@Produces(MediaType.APPLICATION_JSON)
class PurgeProbeLogsResource(
    private val probeRepository: ProbeRepository,
    private val probeMonitorRepository: ProbeMonitorRepository,
) {
    @POST
    @Authenticated
    @Transactional
    fun purgeProbeLogs(
        @PathParam("probeId") probeId: String,
    ): Response {
        val uuid = UUID.fromString(probeId)

        if (probeRepository.findByIdOrNull(uuid) == null) {
            throw NotFoundException("Probe not found")
        }

        probeMonitorRepository.deleteByProbe(uuid)

        return Response.noContent().build()
    }
}
