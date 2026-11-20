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

    fun putJson(
        url: String,
        json: String,
        headers: Map<String, String> = emptyMap(),
    ): HttpRequest =
        request(url)
            .apply { headers.forEach { (name, value) -> header(name, value) } }
            .PUT(HttpRequest.BodyPublishers.ofString(json))
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

            if (response.statusCode() == HTTP_TOO_MANY_REQUESTS) {
                val retryAfter = retryAfter(response.headers().firstValue("Retry-After").orElse(null))
                throw NotificationRateLimitedException("$channel is rate limiting, retrying in ${retryAfter.toMillis()} ms", retryAfter)
            }

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

    internal companion object {
        const val MAX_ERROR_BODY_LENGTH = 500
        const val HTTP_TOO_MANY_REQUESTS = 429
        private val DEFAULT_RETRY_AFTER = Duration.ofSeconds(5)
        private val MIN_RETRY_AFTER = Duration.ofSeconds(1)
        private val MAX_RETRY_AFTER = Duration.ofMinutes(5)

        fun retryAfter(header: String?): Duration {
            val seconds = header?.trim()?.toBigDecimalOrNull() ?: return DEFAULT_RETRY_AFTER
            val delay = Duration.ofMillis(seconds.movePointRight(3).toLong())
            return delay.coerceIn(MIN_RETRY_AFTER, MAX_RETRY_AFTER)
        }
    }
}
