package tmenier.fr.announcements.resources

import io.quarkus.security.Authenticated
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.announcements.actions.StoreStatusAnnouncementAction
import tmenier.fr.announcements.requests.StoreStatusAnnouncementRequest

@Path("/api/announcements/new")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class StoreStatusAnnouncementResource(
    private val storeStatusAnnouncementAction: StoreStatusAnnouncementAction,
) {
    @POST
    @Authenticated
    fun store(
        @Valid payload: StoreStatusAnnouncementRequest,
    ): Response = Response.ok(mapOf("id" to storeStatusAnnouncementAction.execute(payload))).build()
}
