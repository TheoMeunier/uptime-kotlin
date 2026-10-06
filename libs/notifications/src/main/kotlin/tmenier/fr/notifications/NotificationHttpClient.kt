package tmenier.fr.notifications

import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@ApplicationScoped
class NotificationHttpClient(
    @param:ConfigProperty(name = "notifications.http.connect-timeout", defaultValue = "PT5S")
    val connectTimeout: Duration,
    @param:ConfigProperty(name = "notifications.http.request-timeout", defaultValue = "PT10S")
    val requestTimeout: Duration,
) {
    private val client: HttpClient =
        HttpClient
            .newBuilder()
            .connectTimeout(connectTimeout)
            .build()

    fun request(url: String): HttpRequest.Builder =
        HttpRequest
            .newBuilder()
            .uri(URI.create(url))
            .timeout(requestTimeout)
            .header("Content-Type", "application/json")

    fun postJson(
        url: String,
        json: String,
        headers: Map<String, String> = emptyMap(),
    ): HttpRequest =
        request(url)
            .apply { headers.forEach { (name, value) -> header(name, value) } }
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build()

    fun send(request: HttpRequest): HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

    fun deliver(
        channel: String,
        request: HttpRequest,
        describeError: (String) -> String = { it.take(MAX_ERROR_BODY_LENGTH) },
    ) {
        try {
            val response = send(request)
            logger.info { "$channel answered HTTP ${response.statusCode()}" }

            check(response.statusCode() in 200..299) {
                "$channel returned HTTP ${response.statusCode()}: ${describeError(response.body())}"
            }

            logger.info { "$channel notification sent successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Exception while sending $channel notification: ${e.message}" }
            throw e
        }
    }

    @PreDestroy
    fun close() {
        client.shutdownNow()
    }

    private companion object {
        const val MAX_ERROR_BODY_LENGTH = 500
    }
}
