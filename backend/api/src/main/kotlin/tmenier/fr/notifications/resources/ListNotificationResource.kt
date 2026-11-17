package tmenier.fr.notifications.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.mappers.NotificationMapper
import tmenier.fr.databases.repositories.NotificationRepository

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListNotificationResource(
    private val notificationsChannelRepository: NotificationRepository,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun list(): Response {
        val notifications =
            notificationsChannelRepository.getAll().map {
                NotificationMapper.toSmallDto(it)
            }

        return Response.ok(notifications).build()
    }
}
