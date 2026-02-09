package tmenier.fr.monitors.resources.notifications

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.monitors.actions.notifications.TestingNotificationAction
import tmenier.fr.monitors.dtos.requests.BaseStoreNotificationRequest

@Path("/api/notifications/testing")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class TestingNotificationResource(
    private val testingNotificationAction: TestingNotificationAction,
) {
    @POST
    @Authenticated
    @Transactional
    fun store(
        @Valid payload: BaseStoreNotificationRequest,
    ): Response {
        testingNotificationAction.execute(payload)

        return Response.ok().build()
    }
}
