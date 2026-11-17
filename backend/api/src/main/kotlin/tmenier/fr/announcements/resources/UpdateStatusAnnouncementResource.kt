package tmenier.fr.announcements.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.announcements.actions.StoreStatusAnnouncementAction
import tmenier.fr.announcements.requests.StoreStatusAnnouncementRequest
import tmenier.fr.common.enums.users.UserRole
import java.util.UUID

@Path("/api/announcements/{announcementId}/update")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateStatusAnnouncementResource(
    private val storeStatusAnnouncementAction: StoreStatusAnnouncementAction,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    fun update(
        @PathParam("announcementId") announcementId: UUID,
        @Valid payload: StoreStatusAnnouncementRequest,
    ): Response = Response.ok(mapOf("id" to storeStatusAnnouncementAction.execute(payload, announcementId))).build()
}
