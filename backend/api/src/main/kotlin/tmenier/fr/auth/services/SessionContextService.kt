package tmenier.fr.auth.services

import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.core.HttpHeaders

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
