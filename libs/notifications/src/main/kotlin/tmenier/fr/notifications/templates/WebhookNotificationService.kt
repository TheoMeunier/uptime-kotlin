package tmenier.fr.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.HttpMethodEnum
import tmenier.fr.monitors.enums.NotificationChannelsEnum
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime

@ApplicationScoped
class WebhookNotificationService : tmenier.fr.notifications.TypedNotificationInterfaces<NotificationContent.Webhook> {
    private val client = HttpClient.newHttpClient()

    override fun sendSuccess(
        content: NotificationContent.Webhook,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val payload = buildPayload(probe.name, result.message, result.runAt, result.status)
        sendWebhook(content, payload)
    }

    override fun sendFailure(
        content: NotificationContent.Webhook,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val payload = buildPayload(probe.name, result.message, result.runAt, result.status)
        sendWebhook(content, payload)
    }

    override fun sendTest(content: NotificationContent.Webhook) {
        sendWebhook(
            content,
            buildPayload("Test", "Test notification", LocalDateTime.now(), ProbeMonitorLogStatus.SUCCESS),
        )
    }

    override fun getNotificationType() = NotificationChannelsEnum.WEBHOOK.name

    private fun buildPayload(
        name: String,
        message: String,
        runAt: LocalDateTime,
        status: ProbeMonitorLogStatus,
    ): String {
        val escapedName = name.replace("\"", "\\\"").replace("\n", "\\n")
        val escapedMessage = message.replace("\"", "\\\"").replace("\n", "\\n")

        return """
            {
                "name": "$escapedName",
                "status": "${status.name}",
                "message": "$escapedMessage",
                "runAt": "$runAt"
            }
            """.trimIndent()
    }

    private fun sendWebhook(
        content: NotificationContent.Webhook,
        jsonPayload: String,
    ): WebhookResponse =
        try {
            logger.info { "Sending Webhook notification to: ${content.url.take(50)}... [${content.method}]" }
            logger.debug { "Payload: $jsonPayload" }

            val bodyPublisher =
                when (content.method) {
                    HttpMethodEnum.GET -> HttpRequest.BodyPublishers.noBody()
                    else -> HttpRequest.BodyPublishers.ofString(jsonPayload)
                }

            val requestBuilder =
                HttpRequest
                    .newBuilder()
                    .uri(URI.create(content.url))
                    .header("Content-Type", "application/json")
                    .method(content.method.toString().uppercase(), bodyPublisher)

            val response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())

            logger.info { "Webhook response: ${response.statusCode()} - ${response.body()}" }

            val success = response.statusCode() in 200..299
            if (success) {
                logger.info { "Webhook notification sent successfully" }
            } else {
                logger.warn { "Webhook returned non-2xx status: ${response.statusCode()}" }
            }

            WebhookResponse(
                success = success,
                statusCode = response.statusCode(),
                body = response.body(),
            )
        } catch (e: Exception) {
            logger.error(e) { "Exception while sending Webhook notification: ${e.message}" }
            WebhookResponse(
                success = false,
                statusCode = null,
                body = null,
                error = e.message,
            )
        }
}

data class WebhookResponse(
    val success: Boolean,
    val statusCode: Int?,
    val body: String?,
    val error: String? = null,
)
