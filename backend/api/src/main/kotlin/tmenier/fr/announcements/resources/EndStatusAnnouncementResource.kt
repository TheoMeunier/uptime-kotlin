package tmenier.fr.announcements.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.announcements.actions.EndStatusAnnouncementAction
import tmenier.fr.common.enums.users.UserRole
import java.util.UUID

@Path("/api/announcements/{announcementId}/end")
@Produces(MediaType.APPLICATION_JSON)
class EndStatusAnnouncementResource(
    private val endStatusAnnouncementAction: EndStatusAnnouncementAction,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun end(
        @PathParam("announcementId") announcementId: UUID,
    ): Response = Response.ok(endStatusAnnouncementAction.execute(announcementId)).build()
}
