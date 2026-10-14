package tmenier.fr.demo

import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.PreMatching
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import tmenier.fr.common.config.DemoMode
import tmenier.fr.common.exceptions.core.ApiErrorResponse

@Provider
@PreMatching
class DemoReadOnlyFilter(
    private val demoMode: DemoMode,
) : ContainerRequestFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        if (!demoMode.enabled) return

        val path = "/" + requestContext.uriInfo.path.trimStart('/')
        if (isAllowed(requestContext.method, path)) return

        requestContext.abortWith(
            Response
                .status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(
                    ApiErrorResponse(
                        error = ERROR_CODE,
                        message = "This is a read-only demo: changes are disabled.",
                        path = path,
                    ),
                ).build(),
        )
    }

    companion object {
        const val ERROR_CODE = "DEMO_READ_ONLY"

        private val READ_METHODS = setOf("GET", "HEAD", "OPTIONS")

        private val ALLOWED_WRITES =
            listOf(
                Regex("^/api/auth/(login|refresh|logout)/?$"),
                Regex("^/api/probes/[^/]+/check-now/?$"),
            )

        fun isAllowed(
            method: String,
            path: String,
        ): Boolean {
            if (method.uppercase() in READ_METHODS) return true

            return ALLOWED_WRITES.any { it.matches(path) }
        }
    }
}
