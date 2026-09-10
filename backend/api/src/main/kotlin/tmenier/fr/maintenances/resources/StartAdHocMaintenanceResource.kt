package tmenier.fr.maintenances.resources

import io.quarkus.security.Authenticated
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.maintenances.actions.StartAdHocMaintenanceAction
import tmenier.fr.maintenances.requests.StartAdHocMaintenanceRequest
import java.util.UUID

@Path("/api/probes/{probeId}/maintenance")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class StartAdHocMaintenanceResource(
    private val startAdHocMaintenanceAction: StartAdHocMaintenanceAction,
) {
    @POST
    @Authenticated
    fun start(
        @PathParam("probeId") probeId: UUID,
        @Valid payload: StartAdHocMaintenanceRequest,
    ): Response = Response.ok(mapOf("id" to startAdHocMaintenanceAction.execute(probeId, payload))).build()
}
