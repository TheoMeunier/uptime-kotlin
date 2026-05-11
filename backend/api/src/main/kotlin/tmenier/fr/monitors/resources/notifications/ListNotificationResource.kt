package tmenier.fr.monitors.resources.notifications

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.monitors.entities.NotificationsChannelEntity
import tmenier.fr.monitors.entities.mapper.toListingsDTO

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListNotificationResource {
    @GET
    @Authenticated
    fun list(): Response {
        val notifications =
            NotificationsChannelEntity.getAll().map {
                it.toListingsDTO()
            }

        return Response.ok(notifications).build()
    }
}
