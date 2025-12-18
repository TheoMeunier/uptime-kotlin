package tmenier.fr.monitors.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.monitors.actions.OnOffProbeMonitorAction
import tmenier.fr.monitors.dtos.requests.OnOffRequest
import java.util.*

@Path("/api/probes/{probeId}/update-on-off")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class OnOffMonitorProbeResource(
    private val onOffProbeMonitorAction: OnOffProbeMonitorAction
) {

    @POST
    @Authenticated
    @Transactional
    fun onOffProbeMonitor(
        @PathParam("probeId") probeId: String,
        @Valid payload: OnOffRequest
    ): Response {
        onOffProbeMonitorAction.execute(UUID.fromString(probeId), payload.enabled)

        return Response.ok().build()
    }
}