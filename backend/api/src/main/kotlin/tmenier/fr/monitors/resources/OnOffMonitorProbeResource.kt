package tmenier.fr.monitors.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.monitors.actions.OnOffProbeMonitorAction
import tmenier.fr.monitors.requests.OnOffRequest
import java.util.UUID

@Path("/api/probes/{probeId}/update-on-off")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class OnOffMonitorProbeResource(
    private val onOffProbeMonitorAction: OnOffProbeMonitorAction,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    @Transactional
    fun onOffProbeMonitor(
        @PathParam("probeId") probeId: UUID,
        @Valid payload: OnOffRequest,
    ): Response {
        onOffProbeMonitorAction.execute(probeId, payload.enabled)

        return Response.ok().build()
    }
}
