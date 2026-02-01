package tmenier.fr.monitors.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.logger
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.NotificationChannelsEnum
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.notifications.TypedNotificationInterfaces
import tmenier.fr.monitors.notifications.dto.NotificationContent
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ApplicationScoped
class SlackNotificationService : TypedNotificationInterfaces<NotificationContent.Slack> {
    private val client = HttpClient.newHttpClient()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    override fun sendSuccess(
        content: NotificationContent.Slack,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val jsonPayload =
            buildSlackMessage(
                "Service ${probe.name} - ${result.status}",
                result.message,
                "#00FF00",
                result.runAt,
                result.status,
            )
        sendSlackNotification(content, jsonPayload)
    }

    override fun sendFailure(
        content: NotificationContent.Slack,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val jsonPayload =
            buildSlackMessage(
                "Service ${probe.name} - ${result.status}",
                result.message,
                "#FF0000",
                result.runAt,
                result.status,
            )
        sendSlackNotification(content, jsonPayload)
    }

    override fun sendTest(content: NotificationContent.Slack) {
        sendSlackNotification(
            content,
            buildSlackMessage(
                "Test notification",
                "Test notification",
                "#0078D4",
                LocalDateTime.now(),
                ProbeMonitorLogStatus.SUCCESS,
            ),
        )
    }

    override fun getNotificationType() = NotificationChannelsEnum.SLACK.name

    private fun buildSlackMessage(
        title: String,
        message: String,
        color: String,
        runAt: LocalDateTime,
        status: ProbeMonitorLogStatus,
    ): String {
        val escapedTitle = escapeJson(title)
        val escapedMessage = escapeJson(message)
        val formattedDate = runAt.format(dateFormatter)

        // Déterminer l'emoji selon le statut
        val emoji =
            when (status) {
                ProbeMonitorLogStatus.SUCCESS -> ":white_check_mark:"
                ProbeMonitorLogStatus.FAILURE -> ":x:"
                else -> ":information_source:"
            }

        return """
            {
                "attachments": [
                    {
                        "color": "$color",
                        "blocks": [
                            {
                                "type": "header",
                                "text": {
                                    "type": "plain_text",
                                    "text": "$emoji $escapedTitle",
                                    "emoji": true
                                }
                            },
                            {
                                "type": "section",
                                "text": {
                                    "type": "mrkdwn",
                                    "text": "$escapedMessage"
                                }
                            },
                            {
                                "type": "section",
                                "fields": [
                                    {
                                        "type": "mrkdwn",
                                        "text": "*Statut:*\n$status"
                                    },
                                    {
                                        "type": "mrkdwn",
                                        "text": "*Date:*\n$formattedDate"
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
            """.trimIndent()
    }

    private fun sendSlackNotification(
        content: NotificationContent.Slack,
        jsonPayload: String,
    ) {
        try {
            val request =
                HttpRequest
                    .newBuilder()
                    .uri(URI.create(content.webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            logger.info { "Slack API response: ${response.statusCode()}" }

            when (response.statusCode()) {
                200 -> {
                    logger.info { "Slack notification sent successfully" }
                }

                else -> {
                    logger.error { "Slack notification failed with status: ${response.statusCode()}, body: ${response.body()}" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Exception while sending Slack notification: ${e.message}" }
        }
    }

    private fun escapeJson(text: String): String =
        text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
}
