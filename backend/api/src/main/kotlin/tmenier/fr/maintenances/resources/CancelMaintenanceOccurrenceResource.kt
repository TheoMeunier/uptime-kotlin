package tmenier.fr.maintenances.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.repositories.MaintenanceOccurrenceRepository
import java.util.UUID

@Path("/api/maintenances/occurrences/{occurrenceId}/cancel")
@Produces(MediaType.APPLICATION_JSON)
class CancelMaintenanceOccurrenceResource(
    private val maintenanceOccurrenceRepository: MaintenanceOccurrenceRepository,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun cancel(
        @PathParam("occurrenceId") occurrenceId: UUID,
    ): Response {
        if (!maintenanceOccurrenceRepository.cancel(occurrenceId)) {
            throw NotFoundException("Maintenance occurrence not found: $occurrenceId")
        }

        return Response.noContent().build()
    }
}
