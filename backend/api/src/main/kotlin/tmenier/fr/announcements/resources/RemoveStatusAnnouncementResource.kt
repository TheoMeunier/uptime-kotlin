package tmenier.fr.announcements.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.databases.repositories.StatusAnnouncementRepository
import java.util.UUID

@Path("/api/announcements/{announcementId}/remove")
@Produces(MediaType.APPLICATION_JSON)
class RemoveStatusAnnouncementResource(
    private val statusAnnouncementRepository: StatusAnnouncementRepository,
) {
    @POST
    @Authenticated
    @Transactional
    fun remove(
        @PathParam("announcementId") announcementId: UUID,
    ): Response {
        statusAnnouncementRepository.delete(announcementId)

        return Response.noContent().build()
    }
}
