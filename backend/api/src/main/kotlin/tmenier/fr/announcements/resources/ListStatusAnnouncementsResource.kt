package tmenier.fr.announcements.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.announcements.actions.ListStatusAnnouncementAction

@Path("/api/announcements")
@Produces(MediaType.APPLICATION_JSON)
class ListStatusAnnouncementsResource(
    private val listStatusAnnouncementAction: ListStatusAnnouncementAction,
) {
    @GET
    @Authenticated
    fun list(): Response = Response.ok(listStatusAnnouncementAction.execute()).build()
}
