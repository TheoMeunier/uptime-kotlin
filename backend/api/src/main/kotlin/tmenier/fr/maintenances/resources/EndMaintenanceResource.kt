package tmenier.fr.maintenances.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.repositories.MaintenanceOccurrenceRepository
import java.time.Instant
import java.util.UUID

@Path("/api/maintenances/{maintenanceId}/end")
@Produces(MediaType.APPLICATION_JSON)
class EndMaintenanceResource(
    private val maintenanceOccurrenceRepository: MaintenanceOccurrenceRepository,
) {
    @POST
    @Authenticated
    fun end(
        @PathParam("maintenanceId") maintenanceId: UUID,
    ): Response {
        val ended = maintenanceOccurrenceRepository.endNow(maintenanceId, Instant.now())

        return Response.ok(mapOf("ended" to ended)).build()
    }
}
