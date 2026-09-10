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
import tmenier.fr.maintenances.actions.StoreMaintenanceAction
import tmenier.fr.maintenances.requests.StoreMaintenanceRequest
import java.util.UUID

@Path("/api/maintenances/{maintenanceId}/update")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateMaintenanceResource(
    private val storeMaintenanceAction: StoreMaintenanceAction,
) {
    @POST
    @Authenticated
    fun update(
        @PathParam("maintenanceId") maintenanceId: UUID,
        @Valid payload: StoreMaintenanceRequest,
    ): Response = Response.ok(mapOf("id" to storeMaintenanceAction.execute(payload, maintenanceId))).build()
}
