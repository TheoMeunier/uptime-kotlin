package tmenier.fr.monitors.resources.notifications

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.utils.logger
import tmenier.fr.monitors.entities.NotificationsChannelEntity
import java.util.UUID

@Path("/api/notifications/{id}/remove")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RemoveNotification {
    @POST
    @Authenticated
    @Transactional
    fun remove(
        @PathParam("id") id: UUID,
    ): Response {
        try {
            NotificationsChannelEntity.findById(id).let {
                it?.delete()
            }

            return Response.status(Response.Status.NO_CONTENT).build()
        } catch (e: Exception) {
            logger.error(e) { "Failed to remove notification ${e.message}" }
            return Response.serverError().build()
        }
    }
}
