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

@ApplicationScoped
class DiscordNotificationService : TypedNotificationInterfaces<NotificationContent.Discord> {
    private val client = HttpClient.newHttpClient()

    override fun sendSuccess(
        content: NotificationContent.Discord,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val jsonPayload = buildEmbed(probe.name, result.message, 0x00FF00, result.runAt, result.status)
        sendDiscordEmbed(content, jsonPayload)
    }

    override fun sendFailure(
        content: NotificationContent.Discord,
        probe: ProbesEntity,
        result: ProbeResult,
    ) {
        val jsonPayload = buildEmbed(probe.name, result.message, 0xFF0000, result.runAt, result.status)
        sendDiscordEmbed(content, jsonPayload)
    }

    override fun sendTest(content: NotificationContent.Discord) {
        sendDiscordEmbed(
            content,
            buildEmbed("Test", "Test notification", 0x0000FF, LocalDateTime.now(), ProbeMonitorLogStatus.SUCCESS),
        )
    }

    override fun getNotificationType() = NotificationChannelsEnum.DISCORD.name

    private fun buildEmbed(
        title: String,
        description: String,
        color: Int,
        runAt: LocalDateTime,
        status: ProbeMonitorLogStatus,
    ): String {
        val escapedTitle = title.replace("\"", "\\\"").replace("\n", "\\n")
        val escapedDescription = description.replace("\"", "\\\"").replace("\n", "\\n")

        return """
            {
                "embeds": [
                    {
                        "title": "Your service $escapedTitle is $status",
                        "description": "$escapedDescription",
                        "color": $color
                    }
                ]
            }
            """.trimIndent()
    }

    private fun sendDiscordEmbed(
        content: NotificationContent.Discord,
        jsonPayload: String,
    ) {
        try {
            logger.info { "Sending Discord notification to: ${content.webhookUrl.take(50)}..." }
            logger.debug { "Payload: $jsonPayload" }

            val request =
                HttpRequest
                    .newBuilder()
                    .uri(URI.create(content.webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            logger.info { "Discord API response: ${response.statusCode()}" }

            when (response.statusCode()) {
                204, 200 -> {
                    logger.info { "Discord notification sent successfully" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Exception while sending Discord notification: ${e.message}" }
        }
    }
}
