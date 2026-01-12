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
class TeamsNotificationService : TypedNotificationInterfaces<NotificationContent.Teams> {
    private val client = HttpClient.newHttpClient()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    override fun sendSuccess(
        content: NotificationContent.Teams,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val jsonPayload =
            buildTeamsMessage(
                "Service ${probe.name} - ${result.status}",
                result.message,
                "00FF00",
                result.runAt,
                result.status,
            )
        sendTeamsNotification(content, jsonPayload)
    }

    override fun sendFailure(
        content: NotificationContent.Teams,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val jsonPayload =
            buildTeamsMessage(
                "Service ${probe.name} - ${result.status}",
                result.message,
                "FF0000",
                result.runAt,
                result.status,
            )
        sendTeamsNotification(content, jsonPayload)
    }

    override fun sendTest(content: NotificationContent.Teams) {
        sendTeamsNotification(
            content,
            buildTeamsMessage(
                "Test notification",
                "Test notification",
                "0078D4",
                LocalDateTime.now(),
                ProbeMonitorLogStatus.SUCCESS,
            ),
        )
    }

    override fun getNotificationType() = NotificationChannelsEnum.TEAMS.name

    private fun buildTeamsMessage(
        title: String,
        message: String,
        themeColor: String,
        runAt: LocalDateTime,
        status: ProbeMonitorLogStatus,
    ): String {
        val escapedTitle = escapeJson(title)
        val escapedMessage = escapeJson(message)
        val formattedDate = runAt.format(dateFormatter)

        return """
            {
                "@type": "MessageCard",
                "@context": "https://schema.org/extensions",
                "themeColor": "$themeColor",
                "title": "$escapedTitle",
                "text": "$escapedMessage",
                "sections": [
                    {
                        "facts": [
                            {
                                "name": "Statut:",
                                "value": "$status"
                            },
                            {
                                "name": "Date:",
                                "value": "$formattedDate"
                            }
                        ]
                    }
                ]
            }
            """.trimIndent()
    }

    private fun sendTeamsNotification(
        content: NotificationContent.Teams,
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

            logger.info { "Teams API response: ${response.statusCode()}" }

            when (response.statusCode()) {
                204, 200 -> {
                    logger.info { "Teams notification sent successfully" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Exception while sending Teams notification: ${e.message}" }
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
