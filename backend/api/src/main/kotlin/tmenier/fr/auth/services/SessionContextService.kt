package tmenier.fr.auth.services

import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.core.HttpHeaders

/**
 * Reads the client fingerprint carried by the current HTTP request so a refresh token
 * can be presented to the user as a readable session ("Firefox on macOS, 1.2.3.4").
 *
 * The IP is resolved from the reverse proxy headers (see docker/nginx.conf): when the API
 * is reached directly, without a proxy, no IP is recorded.
 */
@RequestScoped
class SessionContextService(
    private val headers: HttpHeaders,
) {
    fun userAgent(): String? = headers.getHeaderString(HttpHeaders.USER_AGENT)?.trim()?.takeIf { it.isNotEmpty() }

    fun ipAddress(): String? =
        headers
            .getHeaderString("X-Forwarded-For")
            ?.substringBefore(',')
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: headers.getHeaderString("X-Real-IP")?.trim()?.takeIf { it.isNotEmpty() }
}
