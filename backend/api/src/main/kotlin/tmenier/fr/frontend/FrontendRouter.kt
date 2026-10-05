package tmenier.fr.frontend

import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

@ApplicationScoped
class FrontendRouter {
    fun register(
        @Observes router: Router,
    ) {
        if (!isFrontendEmbedded()) return

        router
            .route("/*")
            .method(HttpMethod.GET)
            .method(HttpMethod.HEAD)
            .handler(::handle)
    }

    private fun handle(ctx: RoutingContext) {
        val path = ctx.normalizedPath()
        if (isBackendPath(path)) {
            ctx.next()
            return
        }

        ctx.addHeadersEndHandler {
            val headers = ctx.response().headers()
            headers.set("X-Content-Type-Options", "nosniff")
            headers.set("X-Frame-Options", "SAMEORIGIN")
            headers.set("Referrer-Policy", "strict-origin-when-cross-origin")
            headers.set("Cache-Control", if (path.startsWith(ASSETS_PREFIX)) IMMUTABLE_CACHE else NO_CACHE)
        }

        if (path == "/" || path.substringAfterLast('/').contains('.')) {
            ctx.next()
        } else {
            ctx.reroute("/")
        }
    }

    private fun isBackendPath(path: String): Boolean = BACKEND_PREFIXES.any { path == it || path.startsWith("$it/") }

    private fun isFrontendEmbedded(): Boolean = Thread.currentThread().contextClassLoader.getResource(INDEX_RESOURCE) != null

    companion object {
        private const val INDEX_RESOURCE = "META-INF/resources/index.html"
        private const val ASSETS_PREFIX = "/assets/"
        private const val IMMUTABLE_CACHE = "public, max-age=31536000, immutable"
        private const val NO_CACHE = "no-cache"
        private val BACKEND_PREFIXES = listOf("/api", "/q")
    }
}
