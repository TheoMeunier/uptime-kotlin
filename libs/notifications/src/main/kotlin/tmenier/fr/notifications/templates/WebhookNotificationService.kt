package tmenier.fr.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.HttpMethodEnum
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.common.enums.notifications.NotificationEvent
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.notifications.JsonText
import tmenier.fr.notifications.NotificationHttpClient
import java.net.http.HttpRequest
import java.time.Duration
import java.time.Instant

@ApplicationScoped
class WebhookNotificationService(
    private val http: NotificationHttpClient,
) : tmenier.fr.notifications.TypedNotificationInterfaces<NotificationContent.Webhook> {
    override fun sendSuccess(
        content: NotificationContent.Webhook,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        val payload =
            buildPayload(
                name = probe.name,
                message = result.message,
                runAt = result.runAt,
                status = result.status,
                event = NotificationEvent.RECOVERY,
                downtime = downtime,
            )
        sendWebhook(content, payload)
    }

    override fun sendFailure(
        content: NotificationContent.Webhook,
        probe: ProbeDTO,
        result: ProbeResult,
    ) {
        val payload = buildPayload(probe.name, result.message, result.runAt, result.status, NotificationEvent.FAILURE)
        sendWebhook(content, payload)
    }

    override fun sendReminder(
        content: NotificationContent.Webhook,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        val payload =
            buildPayload(
                name = probe.name,
                message = result.message,
                runAt = result.runAt,
                status = result.status,
                event = NotificationEvent.REMINDER,
                reminderIndex = reminderIndex,
            )
        sendWebhook(content, payload)
    }

    override fun sendTest(content: NotificationContent.Webhook) {
        sendWebhook(
            content,
            buildPayload(
                "Test",
                "Test notification",
                Instant.now(),
                ProbeMonitorLogStatus.SUCCESS,
                NotificationEvent.NONE,
            ),
        )
    }

    override fun getNotificationType() = NotificationChannelsEnum.WEBHOOK.name

    private fun buildPayload(
        name: String,
        message: String,
        runAt: Instant,
        status: ProbeMonitorLogStatus,
        event: NotificationEvent,
        reminderIndex: Int = 0,
        downtime: Duration? = null,
    ): String {
        val escapedName = JsonText.escape(name)
        val escapedMessage = JsonText.escape(message)

        return """
            {
                "name": "$escapedName",
                "status": "${status.name}",
                "event": "${event.name}",
                "reminderIndex": $reminderIndex,
                "downtimeSeconds": ${downtime?.seconds ?: "null"},
                "message": "$escapedMessage",
                "runAt": "$runAt"
            }
            """.trimIndent()
    }

    private fun sendWebhook(
        content: NotificationContent.Webhook,
        jsonPayload: String,
    ) {
        val body =
            when (content.method) {
                HttpMethodEnum.GET -> HttpRequest.BodyPublishers.noBody()
                else -> HttpRequest.BodyPublishers.ofString(jsonPayload)
            }
        http.deliver("Webhook", http.request(content.url).method(content.method.name, body).build())
    }
}
