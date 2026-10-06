package tmenier.fr.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.notifications.JsonText
import tmenier.fr.notifications.NotificationDateFormatter
import tmenier.fr.notifications.NotificationHttpClient
import tmenier.fr.notifications.resolvers.OutageWindow
import java.time.Duration
import java.time.Instant

@ApplicationScoped
class TeamsNotificationService(
    private val http: NotificationHttpClient,
    private val dates: NotificationDateFormatter,
) : tmenier.fr.notifications.TypedNotificationInterfaces<NotificationContent.Teams> {
    override fun sendSuccess(
        content: NotificationContent.Teams,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        val jsonPayload =
            buildTeamsMessage(
                "Service ${probe.name} - ${result.status}${OutageWindow.suffix(downtime)}",
                result.message,
                "00FF00",
                result.runAt,
                result.status,
            )
        sendTeamsNotification(content, jsonPayload)
    }

    override fun sendFailure(
        content: NotificationContent.Teams,
        probe: ProbeDTO,
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

    override fun sendReminder(
        content: NotificationContent.Teams,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        val jsonPayload =
            buildTeamsMessage(
                "Service ${probe.name} - still ${result.status} (reminder #$reminderIndex)",
                result.message,
                "FF8C00",
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
                Instant.now(),
                ProbeMonitorLogStatus.SUCCESS,
            ),
        )
    }

    override fun getNotificationType() = NotificationChannelsEnum.TEAMS.name

    private fun buildTeamsMessage(
        title: String,
        message: String,
        themeColor: String,
        runAt: Instant,
        status: ProbeMonitorLogStatus,
    ): String {
        val escapedTitle = JsonText.escape(title)
        val escapedMessage = JsonText.escape(message)
        val formattedDate = dates.format(runAt)

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
    ) = http.deliver("Teams", http.postJson(content.webhookUrl, jsonPayload))
}
