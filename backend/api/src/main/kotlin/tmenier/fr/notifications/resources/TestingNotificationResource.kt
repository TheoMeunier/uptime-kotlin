package tmenier.fr.notifications.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.notifications.actions.TestingNotificationAction
import tmenier.fr.notifications.dto.NotificationTestResult
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import java.util.UUID

@Path("/api/notifications/testing")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class TestingNotificationResource(
    private val testingNotificationAction: TestingNotificationAction,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    @Transactional
    fun store(
        @Valid payload: BaseStoreNotificationRequest,
        @QueryParam("notificationId") notificationId: UUID?,
    ): Response =
        when (val result = testingNotificationAction.execute(payload, notificationId)) {
            is NotificationTestResult.Success -> Response.ok().build()
            is NotificationTestResult.Failure ->
                Response
                    .status(422)
                    .entity(mapOf("message" to result.message))
                    .build()
        }
}
