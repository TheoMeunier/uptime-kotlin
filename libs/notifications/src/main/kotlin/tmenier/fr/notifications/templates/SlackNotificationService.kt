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
class SlackNotificationService(
    private val http: NotificationHttpClient,
    private val dates: NotificationDateFormatter,
) : tmenier.fr.notifications.TypedNotificationInterfaces<NotificationContent.Slack> {
    override fun sendSuccess(
        content: NotificationContent.Slack,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        val jsonPayload =
            buildSlackMessage(
                "Service ${probe.name} - ${result.status}${OutageWindow.suffix(downtime)}",
                result.message,
                "#00FF00",
                result.runAt,
                result.status,
            )
        sendSlackNotification(content, jsonPayload)
    }

    override fun sendFailure(
        content: NotificationContent.Slack,
        probe: ProbeDTO,
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

    override fun sendReminder(
        content: NotificationContent.Slack,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        val jsonPayload =
            buildSlackMessage(
                "Service ${probe.name} - still ${result.status} (reminder #$reminderIndex)",
                result.message,
                "#FF8C00",
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
                Instant.now(),
                ProbeMonitorLogStatus.SUCCESS,
            ),
        )
    }

    override fun getNotificationType() = NotificationChannelsEnum.SLACK.name

    private fun buildSlackMessage(
        title: String,
        message: String,
        color: String,
        runAt: Instant,
        status: ProbeMonitorLogStatus,
    ): String {
        val escapedTitle = JsonText.escape(title)
        val escapedMessage = JsonText.escape(message)
        val formattedDate = dates.format(runAt)

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
    ) = http.deliver("Slack", http.postJson(content.webhookUrl, jsonPayload))
}
