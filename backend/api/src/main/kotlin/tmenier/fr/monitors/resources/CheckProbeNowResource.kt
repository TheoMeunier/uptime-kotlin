package tmenier.fr.monitors.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.probes.ImmediateCheckOutcome
import tmenier.fr.monitors.actions.CheckProbeNowAction
import java.util.UUID

@Path("/api/probes/{probeId}/check-now")
@Produces(MediaType.APPLICATION_JSON)
class CheckProbeNowResource(
    private val checkProbeNowAction: CheckProbeNowAction,
) {
    @POST
    @Authenticated
    fun checkNow(
        @PathParam("probeId") probeId: UUID,
    ): Response {
        val outcome = checkProbeNowAction.execute(probeId)
        val body = mapOf("status" to outcome.name.lowercase())

        return when (outcome) {
            ImmediateCheckOutcome.TRIGGERED -> Response.ok(body)
            ImmediateCheckOutcome.ALREADY_RUNNING -> Response.accepted(body)
            ImmediateCheckOutcome.DISABLED -> Response.status(Response.Status.CONFLICT).entity(body)
        }.build()
    }
}
