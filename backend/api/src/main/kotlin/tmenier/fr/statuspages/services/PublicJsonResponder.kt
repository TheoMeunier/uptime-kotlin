package tmenier.fr.statuspages.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.EntityTag
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Request
import jakarta.ws.rs.core.Response
import tmenier.fr.monitors.services.StatusSnapshotService.Companion.CACHE_POLICY
import java.security.MessageDigest
import java.util.Base64

@ApplicationScoped
class PublicJsonResponder(
    private val objectMapper: ObjectMapper,
) {
    fun respond(
        request: Request,
        body: Any,
    ): Response {
        val json = objectMapper.writeValueAsBytes(body)
        val tag = EntityTag(fingerprint(json), true)

        request.evaluatePreconditions(tag)?.let { notModified ->
            return notModified
                .tag(tag)
                .header(HttpHeaders.CACHE_CONTROL, CACHE_POLICY)
                .build()
        }

        return Response
            .ok(json, MediaType.APPLICATION_JSON_TYPE)
            .tag(tag)
            .header(HttpHeaders.CACHE_CONTROL, CACHE_POLICY)
            .build()
    }

    fun fingerprint(json: ByteArray): String =
        Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(MessageDigest.getInstance("SHA-256").digest(json))
            .take(ETAG_LENGTH)

    companion object {
        private const val ETAG_LENGTH = 22
    }
}
