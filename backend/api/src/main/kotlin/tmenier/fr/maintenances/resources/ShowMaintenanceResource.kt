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

@Path("/api/maintenances/{maintenanceId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ShowMaintenanceResource(
    private val listMaintenanceAction: ListMaintenanceAction,
) {
    @GET
    @Authenticated
    fun show(
        @PathParam("maintenanceId") maintenanceId: UUID,
    ): Response = Response.ok(listMaintenanceAction.show(maintenanceId)).build()
}
