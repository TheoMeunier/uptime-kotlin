package tmenier.fr.monitors.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.logger
import tmenier.fr.monitors.dtos.responses.ProbeMonitorDTO
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.NotificationChannelsEnum
import tmenier.fr.monitors.notifications.TypedNotificationInterfaces
import tmenier.fr.monitors.notifications.dto.NotificationContent
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@ApplicationScoped
class DiscordNotificationService : TypedNotificationInterfaces<NotificationContent.Discord> {

    private val client = HttpClient.newHttpClient()

    override fun sendSuccess(
        content: NotificationContent.Discord,
        probe: ProbesEntity,
        result: ProbeMonitorDTO
    ) {
        val jsonPayload = buildEmbed(probe.name, result.message, 0x00FF00)
        sendDiscordEmbed(content, jsonPayload)
    }

    override fun sendFailure(
        content: NotificationContent.Discord,
        probe: ProbesEntity,
        result: ProbeMonitorDTO
    ) {
        val jsonPayload = buildEmbed(probe.name, result.message, 0xFF0000)
        sendDiscordEmbed(content, jsonPayload)
    }

    override fun sendTest(content: NotificationContent.Discord) {
        sendDiscordEmbed(content, buildEmbed("Test", "Test", 0x0000FF))
    }

    override fun getNotificationType() = NotificationChannelsEnum.DISCORD.name

    private fun buildEmbed(title: String, description: String, color: Int): String {
        return """
            {
                "embeds": [
                    {
                        "title": "$title",
                        "description": "$description",
                        "color": $color
                    }
                ]
            }
        """.trimIndent()
    }

    private fun sendDiscordEmbed(content: NotificationContent.Discord, jsonPayload: String) {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(content.webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 204) {
                logger.error("Failed to send Discord notification: ${response.statusCode()} ${response.body()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}