package tmenier.fr.statuspages.resources

import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.Response
import tmenier.fr.statuspages.domains.CustomDomainRegistry
import tmenier.fr.statuspages.domains.RequestHost

@Path("/api/status-domains/allowed")
class AllowedStatusDomainResource(
    private val registry: CustomDomainRegistry,
) {
    @GET
    @PermitAll
    fun allowed(
        @QueryParam("domain") domain: String?,
    ): Response =
        if (registry.isCustomDomain(RequestHost.normalize(domain))) {
            Response.ok().build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
}
