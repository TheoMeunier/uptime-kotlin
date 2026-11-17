package tmenier.fr.monitors.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.groups.ConvertGroup
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.monitors.actions.StoreProbeAction
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.requests.OnCreate

@Path("/api/probes/new")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CreateProbeResource(
    private val storeProbeAction: StoreProbeAction,
) {
    @POST
    @RolesAllowed(UserRole.ADMIN_ROLE)
    @Transactional
    fun store(
        @Valid @ConvertGroup(to = OnCreate::class) payload: BaseStoreProbeRequest,
    ): Response {
        storeProbeAction.execute(payload)

        return Response.ok().build()
    }
}
