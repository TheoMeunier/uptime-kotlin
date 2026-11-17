package tmenier.fr.monitors.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.repositories.ProbePushStateRepository
import java.util.UUID

@Path("/api/probes/{probeId}/push/regenerate")
@Produces(MediaType.APPLICATION_JSON)
class RegeneratePushTokenResource(
    private val probePushStateRepository: ProbePushStateRepository,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun regenerate(
        @PathParam("probeId") probeId: UUID,
    ): Response {
        val token =
            probePushStateRepository.regenerateToken(probeId)
                ?: throw NotFoundException("Push monitor not found")

        return Response.ok(mapOf("token" to token)).build()
    }
}
