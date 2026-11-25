package tmenier.fr.statuspages.domains

import io.vertx.core.http.HttpServerRequest

object RequestHost {
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
