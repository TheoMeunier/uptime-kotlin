package tmenier.fr.auth.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.auth.actions.RevokeSessionAction
import tmenier.fr.common.exceptions.common.NotFoundException
import java.util.UUID

@Path("/api/auth/sessions/{sessionId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class RevokeSessionResource(
    private val revokeSessionAction: RevokeSessionAction,
) {
    @DELETE
    @Transactional
    @Authenticated
    fun revoke(
        @PathParam("sessionId") sessionId: String,
    ): Response {
        val id =
            runCatching { UUID.fromString(sessionId) }
                .getOrElse { throw NotFoundException("Session not found") }

        revokeSessionAction.execute(id)

        return Response.noContent().build()
    }
}
