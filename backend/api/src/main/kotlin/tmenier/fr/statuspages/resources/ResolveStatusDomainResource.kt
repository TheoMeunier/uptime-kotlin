package tmenier.fr.statuspages.resources

import io.vertx.core.http.HttpServerRequest
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.statuspages.domains.CustomDomainRegistry
import tmenier.fr.statuspages.domains.RequestHost

@Path("/api/status-domains/resolve")
@Produces(MediaType.APPLICATION_JSON)
class ResolveStatusDomainResource(
    private val registry: CustomDomainRegistry,
) {
    @GET
    @PermitAll
    fun resolve(
        @Context request: HttpServerRequest,
    ): Response {
        val slug =
            registry.slugFor(RequestHost.of(request))
                ?: return Response.status(Response.Status.NOT_FOUND).build()

        return Response
            .ok(mapOf("slug" to slug))
            .header(HttpHeaders.CACHE_CONTROL, "no-cache")
            .build()
    }
}
