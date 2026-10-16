package tmenier.fr.incidents.resources

import io.quarkus.security.Authenticated
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.incidents.actions.UpdateIncidentNoteAction
import tmenier.fr.incidents.requests.UpdateIncidentNoteRequest
import java.util.UUID

@Path("/api/incidents/{incidentId}/note")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateIncidentNoteResource(
    private val updateIncidentNoteAction: UpdateIncidentNoteAction,
) {
    @PUT
    @Authenticated
    fun update(
        @PathParam("incidentId") incidentId: UUID,
        @Valid payload: UpdateIncidentNoteRequest,
    ): Response = Response.ok(updateIncidentNoteAction.execute(incidentId, payload)).build()
}
