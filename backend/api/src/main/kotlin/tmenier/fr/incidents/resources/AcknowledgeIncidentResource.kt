package tmenier.fr.incidents.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.incidents.actions.AcknowledgeIncidentAction
import java.util.UUID

@Path("/api/incidents/{incidentId}/acknowledge")
@Produces(MediaType.APPLICATION_JSON)
class AcknowledgeIncidentResource(
    private val acknowledgeIncidentAction: AcknowledgeIncidentAction,
    private val jwt: JsonWebToken,
) {
    @POST
    @Authenticated
    fun acknowledge(
        @PathParam("incidentId") incidentId: UUID,
    ): Response = Response.ok(acknowledgeIncidentAction.execute(incidentId, UUID.fromString(jwt.name))).build()
}
