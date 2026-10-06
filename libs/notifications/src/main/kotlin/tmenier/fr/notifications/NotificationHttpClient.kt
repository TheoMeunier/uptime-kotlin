package tmenier.fr.notifications

import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
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

    fun send(request: HttpRequest): HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

    @PreDestroy
    fun close() {
        client.shutdownNow()
    }
}
