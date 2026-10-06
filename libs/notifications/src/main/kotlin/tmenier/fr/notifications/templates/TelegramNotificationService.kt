package tmenier.fr.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.notifications.JsonText
import tmenier.fr.notifications.NotificationDateFormatter
import tmenier.fr.notifications.NotificationHttpClient
import tmenier.fr.notifications.TypedNotificationInterfaces
import tmenier.fr.notifications.resolvers.OutageWindow
import java.time.Duration
import java.time.Instant

/**
 * Sends alerts through the Telegram Bot API (`sendMessage`, HTML parse mode).
 *
 * The bot token is part of the request URL: it is never logged nor put in an
 * exception message.
 */
@ApplicationScoped
class TelegramNotificationService(
    private val http: NotificationHttpClient,
    private val dates: NotificationDateFormatter,
    @param:ConfigProperty(name = "notifications.telegram.api-url", defaultValue = "https://api.telegram.org")
    apiUrl: String,
) : TypedNotificationInterfaces<NotificationContent.Telegram> {
    private val apiUrl = apiUrl.trimEnd('/')

    override fun sendSuccess(
        content: NotificationContent.Telegram,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        val title = "Service ${probe.name} is ${result.status}${OutageWindow.suffix(downtime)}"
        send(content, buildMessage("✅", title, result.message, result.status, result.runAt))
    }

    override fun sendFailure(
        content: NotificationContent.Telegram,
        probe: ProbeDTO,
        result: ProbeResult,
    ) {
        val title = "Service ${probe.name} is ${result.status}"
        send(content, buildMessage("🔴", title, result.message, result.status, result.runAt))
    }

    override fun sendReminder(
        content: NotificationContent.Telegram,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        val title = "Service ${probe.name} is still ${result.status} (reminder #$reminderIndex)"
        send(content, buildMessage("🟠", title, result.message, result.status, result.runAt))
    }

    override fun sendTest(content: NotificationContent.Telegram) {
        send(content, buildMessage("🔵", "Test notification", "Test notification", ProbeMonitorLogStatus.SUCCESS, Instant.now()))
    }

    override fun getNotificationType() = NotificationChannelsEnum.TELEGRAM.name

    internal fun buildMessage(
        emoji: String,
        title: String,
        message: String,
        status: ProbeMonitorLogStatus,
        runAt: Instant,
    ): String =
        buildString {
            append("$emoji <b>${escapeHtml(title)}</b>")
            if (message.isNotBlank()) append("\n\n${escapeHtml(message.take(MAX_DETAIL_LENGTH))}")
            append("\n\n<b>Status:</b> $status")
            append("\n<b>Date:</b> ${escapeHtml(dates.format(runAt))}")
        }

    internal fun buildPayload(
        content: NotificationContent.Telegram,
        text: String,
    ): String =
        buildString {
            append("{")
            append("\"chat_id\":\"${JsonText.escape(content.chatId)}\"")
            content.messageThreadId?.let { append(",\"message_thread_id\":$it") }
            append(",\"text\":\"${JsonText.escape(text)}\"")
            append(",\"parse_mode\":\"HTML\"")
            append(",\"disable_web_page_preview\":true")
            append("}")
        }

    private fun send(
        content: NotificationContent.Telegram,
        text: String,
    ) {
        val request = http.postJson("$apiUrl/bot${content.botToken}/sendMessage", buildPayload(content, text))
        http.deliver("Telegram", request, ::describe)
    }

    /** Keeps Telegram's own `description` ("Bad Request: chat not found"…), which is what the user can act on. */
    private fun describe(body: String): String = DESCRIPTION.find(body)?.groupValues?.get(1) ?: body.take(200)

    private fun escapeHtml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    companion object {
        // Telegram rejects messages over 4096 characters; the probe detail is the only unbounded part.
        private const val MAX_DETAIL_LENGTH = 3000
        private val DESCRIPTION = Regex("\"description\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
    }
}
