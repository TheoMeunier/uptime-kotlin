package tmenier.fr.statuspages.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.repositories.StatusPageRepository
import java.util.UUID

@Path("/api/status-pages/{statusPageId}/remove")
@Produces(MediaType.APPLICATION_JSON)
class RemoveStatusPageResource(
    private val statusPageRepository: StatusPageRepository,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    @Transactional
    fun remove(
        @PathParam("statusPageId") statusPageId: UUID,
    ): Response {
        statusPageRepository.delete(statusPageId)

        return Response.noContent().build()
    }
}
