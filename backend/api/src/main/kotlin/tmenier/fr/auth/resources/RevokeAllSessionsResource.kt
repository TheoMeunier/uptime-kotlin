package tmenier.fr.auth.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.auth.actions.RevokeAllSessionsAction

@Path("/api/auth/sessions/revoke-all")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RevokeAllSessionsResource(
    private val revokeAllSessionsAction: RevokeAllSessionsAction,
) {
    @POST
    @Transactional
    @Authenticated
    fun revokeAll(): Response {
        val revoked = revokeAllSessionsAction.execute()

        return Response.ok(mapOf("revoked" to revoked)).build()
    }
}
