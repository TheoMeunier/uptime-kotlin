package tmenier.fr.maintenances.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.maintenances.actions.ListMaintenanceAction
import java.util.UUID

@Path("/api/probes/{probeId}/maintenances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListProbeMaintenanceResource(
    private val listMaintenanceAction: ListMaintenanceAction,
) {
    @GET
    @Authenticated
    fun list(
        @PathParam("probeId") probeId: UUID,
    ): Response = Response.ok(listMaintenanceAction.byProbe(probeId)).build()
}
