package tmenier.fr.monitors.resources.notifications

import io.quarkus.security.Authenticated
import jakarta.validation.Valid
import jakarta.validation.groups.ConvertGroup
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.monitors.actions.notifications.StoreNotificationAction
import tmenier.fr.monitors.dtos.requests.BaseStoreNotificationRequest
import tmenier.fr.monitors.dtos.requests.OnUpdate
import java.util.UUID

@Path("/api/notifications/{id}/update")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateNotificationResource(
    private val storeNotificationAction: StoreNotificationAction,
) {
    @POST
    @Authenticated
    fun update(
        @PathParam("id") id: UUID,
        @Valid @ConvertGroup(to = OnUpdate::class) payload: BaseStoreNotificationRequest,
    ): Response {
        storeNotificationAction.execute(payload, id)

        return Response.ok().build()
    }
}
