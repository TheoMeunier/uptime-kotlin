package tmenier.fr.notifications.resources

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.notifications.webpush.StoredVapidKeySource

@RegisterForReflection
data class WebPushPublicKeyResponse(
    val publicKey: String,
)

@Path("/api/notifications/web-push/public-key")
@Produces(MediaType.APPLICATION_JSON)
class WebPushPublicKeyResource(
    private val vapidKeys: StoredVapidKeySource,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun show(): WebPushPublicKeyResponse = WebPushPublicKeyResponse(vapidKeys.publicKey())
}
