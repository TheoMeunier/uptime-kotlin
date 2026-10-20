package tmenier.fr.announcements.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.announcements.actions.ListStatusAnnouncementAction
import java.util.UUID

@Path("/api/announcements/{announcementId}")
@Produces(MediaType.APPLICATION_JSON)
class ShowStatusAnnouncementResource(
    private val listStatusAnnouncementAction: ListStatusAnnouncementAction,
) {
    @GET
    @Authenticated
    fun show(
        @PathParam("announcementId") announcementId: UUID,
    ): Response = Response.ok(listStatusAnnouncementAction.show(announcementId)).build()
}
