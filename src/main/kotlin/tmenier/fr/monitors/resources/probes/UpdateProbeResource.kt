package tmenier.fr.monitors.resources.probes

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.monitors.actions.StoreProbeAction
import tmenier.fr.monitors.dtos.requests.BaseStoreProbeRequest
import java.util.*

@Path("/api/probes/{probeId}/update")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateProbeResource(
    private val storeProbeAction: StoreProbeAction,
) {
    @POST
    @Authenticated
    @Transactional
    fun store(
        @PathParam("probeId") probeId: UUID,
        @Valid payload: BaseStoreProbeRequest,
    ): Response {
        storeProbeAction.execute(payload, probeId)
        return Response.ok().build()
    }
}
