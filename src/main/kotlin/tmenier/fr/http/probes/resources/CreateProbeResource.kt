package tmenier.fr.http.probes.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.actions.probes.StoreProbeAction
import tmenier.fr.http.probes.dtos.requests.BaseStoreProbeRequest

@Path("/api/probes/new")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CreateProbeResource(
    private val storeProbeAction: StoreProbeAction
) {

    @POST
    @Authenticated
    @Transactional
    fun store(
        @Valid payload: BaseStoreProbeRequest
    ): Response {
        storeProbeAction.execute(payload)

        return Response.ok().build()
    }
}