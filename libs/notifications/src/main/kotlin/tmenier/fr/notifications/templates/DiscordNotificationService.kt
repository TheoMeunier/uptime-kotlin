package tmenier.fr.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.notifications.JsonText
import tmenier.fr.notifications.NotificationHttpClient
import tmenier.fr.notifications.resolvers.OutageWindow
import java.time.Duration
import java.time.Instant

@ApplicationScoped
class DiscordNotificationService(
    private val http: NotificationHttpClient,
) : tmenier.fr.notifications.TypedNotificationInterfaces<NotificationContent.Discord> {
    override fun sendSuccess(
        content: NotificationContent.Discord,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        val jsonPayload =
            buildEmbed(
                probe.name,
                result.message,
                0x00FF00,
                result.runAt,
                result.status,
                headlineSuffix = OutageWindow.suffix(downtime),
            )
        sendDiscordEmbed(content, jsonPayload)
    }

    override fun sendFailure(
        content: NotificationContent.Discord,
        probe: ProbeDTO,
        result: ProbeResult,
    ) {
        val jsonPayload = buildEmbed(probe.name, result.message, 0xFF0000, result.runAt, result.status)
        sendDiscordEmbed(content, jsonPayload)
    }

    override fun sendReminder(
        content: NotificationContent.Discord,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        val jsonPayload = buildEmbed(probe.name, result.message, 0xFF8C00, result.runAt, result.status, reminderIndex)
        sendDiscordEmbed(content, jsonPayload)
    }

    override fun sendTest(content: NotificationContent.Discord) {
        sendDiscordEmbed(
            content,
            buildEmbed("Test", "Test notification", 0x0000FF, Instant.now(), ProbeMonitorLogStatus.SUCCESS),
        )
    }

    override fun getNotificationType() = NotificationChannelsEnum.DISCORD.name

    private fun buildEmbed(
        title: String,
        description: String,
        color: Int,
        runAt: Instant,
        status: ProbeMonitorLogStatus,
        reminderIndex: Int = 0,
        headlineSuffix: String = "",
    ): String {
        val escapedTitle = JsonText.escape(title)
        val escapedDescription = JsonText.escape(description)
        val headline =
            if (reminderIndex > 0) {
                "Your service $escapedTitle is STILL $status (reminder #$reminderIndex)"
            } else {
                "Your service $escapedTitle is $status$headlineSuffix"
            }

        return """
            {
                "embeds": [
                    {
                        "title": "$headline",
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
    ) = http.deliver("Discord", http.postJson(content.webhookUrl, jsonPayload))
}
