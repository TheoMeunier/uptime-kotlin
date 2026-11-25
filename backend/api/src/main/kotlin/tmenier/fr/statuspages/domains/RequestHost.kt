package tmenier.fr.statuspages.domains

import io.vertx.core.http.HttpServerRequest

object RequestHost {
    /**
     * Host the request was sent to, as Quarkus sees it.
     *
     * X-Forwarded-Host (or Forwarded) only replaces the Host header when proxy forwarding is enabled
     * and the request comes from one of `quarkus.http.proxy.trusted-proxies`; a header sent by any
     * other client is ignored. Never read X-Forwarded-Host directly: it picks the custom domain.
     */
    fun of(request: HttpServerRequest): String? = normalize(request.host())

    fun normalize(raw: String?): String? {
        val first = raw?.substringBefore(',')?.trim()?.lowercase()
        if (first.isNullOrEmpty()) return null

        val withoutPort =
            if (first.startsWith("[")) {
                first.substringBefore(']') + "]"
            } else {
                first.substringBefore(':')
            }

        return withoutPort.trimEnd('.').ifEmpty { null }
    }
}
