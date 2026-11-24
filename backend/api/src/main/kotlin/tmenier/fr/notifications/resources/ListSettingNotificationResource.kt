package tmenier.fr.notifications.resources

import io.quarkus.security.identity.SecurityIdentity
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

@Path("/api/notifications/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ListSettingNotificationResource(
    private val notificationRepository: NotificationRepository,
    private val identity: SecurityIdentity,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun listSettings(): Response {
        val revealWebhookUrls = identity.hasRole(UserRole.ADMIN_ROLE)
        val notifications = notificationRepository.getAll().map { NotificationMapper.toShowDto(it, revealWebhookUrls) }

        return Response.ok(notifications).build()
    }
}
