package tmenier.fr.maintenances.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.maintenances.actions.ListMaintenanceAction

@Path("/api/maintenances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListMaintenanceResource(
    private val listMaintenanceAction: ListMaintenanceAction,
) {
    @GET
    @Authenticated
    fun list(): Response = Response.ok(listMaintenanceAction.execute()).build()
}
