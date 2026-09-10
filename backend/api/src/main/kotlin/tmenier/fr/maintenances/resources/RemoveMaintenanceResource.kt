package tmenier.fr.maintenances.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.repositories.MaintenanceWindowRepository
import java.util.UUID

@Path("/api/maintenances/{maintenanceId}/remove")
@Produces(MediaType.APPLICATION_JSON)
class RemoveMaintenanceResource(
    private val maintenanceWindowRepository: MaintenanceWindowRepository,
) {
    @POST
    @Authenticated
    @Transactional
    fun remove(
        @PathParam("maintenanceId") maintenanceId: UUID,
    ): Response {
        maintenanceWindowRepository.delete(maintenanceId)

        return Response.noContent().build()
    }
}
