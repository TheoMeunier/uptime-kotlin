package tmenier.fr.schedulers.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.Base64

fun interface RabbitMqHealthCheck {
    fun check(
        content: ProbeContent.RabbitMq,
        timeoutSeconds: Int,
    )
}

@ApplicationScoped
class HttpRabbitMqHealthCheck(
    private val encryptionService: EncryptionService,
) : RabbitMqHealthCheck {
    override fun check(
        content: ProbeContent.RabbitMq,
        timeoutSeconds: Int,
    ) {
        val timeout = Duration.ofSeconds(timeoutSeconds.toLong())
        val client =
            HttpClient
                .newBuilder()
                .connectTimeout(timeout)
                .build()
        val authorization =
            basicAuthorization(
                content.username,
                encryptionService.decryptIfEncrypted(content.password),
            )

        parseManagementNodes(content.managementNodes).forEach { node ->
            val healthUri = healthUri(node)
            val request =
                HttpRequest
                    .newBuilder(healthUri)
                    .timeout(timeout)
                    .header("Authorization", authorization)
                    .header("Accept", "application/json")
                    .GET()
                    .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            require(response.statusCode() == 200) {
                "RabbitMQ node $node returned HTTP ${response.statusCode()}: ${response.body()}"
            }
        }
    }

    internal fun parseManagementNodes(managementNodes: String): List<String> {
        val nodes =
            managementNodes
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
        require(nodes.isNotEmpty()) { "At least one RabbitMQ management node is required" }
        return nodes
    }

    internal fun healthUri(node: String): URI {
        val uri = URI(node)
        require(uri.scheme == "http" || uri.scheme == "https") {
            "RabbitMQ management node must start with http:// or https://"
        }
        require(!uri.host.isNullOrBlank()) { "RabbitMQ management node host is required" }

        val basePath = uri.path.trimEnd('/')
        val healthPath =
            if (basePath.endsWith(HEALTH_PATH)) {
                basePath
            } else {
                "$basePath$HEALTH_PATH"
            }

        return URI(
            uri.scheme,
            null,
            uri.host,
            uri.port,
            healthPath,
            null,
            null,
        )
    }

    private fun basicAuthorization(
        username: String,
        password: String,
    ): String {
        val credentials =
            Base64
                .getEncoder()
                .encodeToString("$username:$password".toByteArray(StandardCharsets.UTF_8))
        return "Basic $credentials"
    }

    private companion object {
        const val HEALTH_PATH = "/api/health/checks/alarms"
    }
}
