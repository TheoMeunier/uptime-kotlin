package tmenier.fr.notifications.resources

import io.quarkus.security.Authenticated
import jakarta.validation.Valid
import jakarta.validation.groups.ConvertGroup
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.notifications.actions.StoreNotificationAction
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import tmenier.fr.notifications.requests.OnCreate

@Path("/api/notifications/new")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class StoreNotificationResource(
    private val storeNotificationAction: StoreNotificationAction,
) {
    @POST
    @Authenticated
    fun store(
        @Valid @ConvertGroup(to = OnCreate::class) payload: BaseStoreNotificationRequest,
    ): Response {
        storeNotificationAction.execute(payload)

        return Response.ok().build()
    }
}
