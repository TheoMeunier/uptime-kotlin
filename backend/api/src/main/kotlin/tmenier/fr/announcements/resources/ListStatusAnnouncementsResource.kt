package tmenier.fr.announcements.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.announcements.actions.ListStatusAnnouncementAction
import tmenier.fr.common.enums.users.UserRole

@Path("/api/announcements")
@Produces(MediaType.APPLICATION_JSON)
class ListStatusAnnouncementsResource(
    private val listStatusAnnouncementAction: ListStatusAnnouncementAction,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun list(): Response = Response.ok(listStatusAnnouncementAction.execute()).build()
}
