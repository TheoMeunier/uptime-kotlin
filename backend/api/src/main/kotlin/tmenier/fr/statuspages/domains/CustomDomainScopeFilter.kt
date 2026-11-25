package tmenier.fr.statuspages.domains

import io.vertx.core.http.HttpServerRequest
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.PreMatching
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import tmenier.fr.common.exceptions.core.ApiErrorResponse

@Provider
@PreMatching
class CustomDomainScopeFilter(
    private val registry: CustomDomainRegistry,
    private val request: HttpServerRequest,
) : ContainerRequestFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        val host = RequestHost.of(request)
        if (!registry.isCustomDomain(host)) return

        val path = "/" + requestContext.uriInfo.path.trimStart('/')
        if (isAllowed(requestContext.method, path)) return

        requestContext.abortWith(
            Response
                .status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(ApiErrorResponse(error = ERROR_CODE, message = "Not available on this domain.", path = path))
                .build(),
        )
    }

    companion object {
        const val ERROR_CODE = "NOT_AVAILABLE_ON_CUSTOM_DOMAIN"

        private val READ_METHODS = setOf("GET", "HEAD", "OPTIONS")

        private val ALLOWED_PATHS =
            listOf(
                Regex("^/api/status/[^/]+(/(uptime|feed))?/?$"),
                Regex("^/api/status-domains/resolve/?$"),
            )

        fun isAllowed(
            method: String,
            path: String,
        ): Boolean =
            method.uppercase() in READ_METHODS &&
                ".." !in path &&
                ALLOWED_PATHS.any { it.matches(path) }
    }
}
