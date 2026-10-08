package tmenier.fr.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.encryption.EncryptionService
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

@ApplicationScoped
class GotifyNotificationService(
    private val http: NotificationHttpClient,
    private val dates: NotificationDateFormatter,
    private val encryption: EncryptionService,
) : TypedNotificationInterfaces<NotificationContent.Gotify> {
    override fun sendSuccess(
        content: NotificationContent.Gotify,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        val title = "Service ${probe.name} is ${result.status}${OutageWindow.suffix(downtime)}"
        send(content, buildPayload(title, result, PRIORITY_DEFAULT))
    }

    override fun sendFailure(
        content: NotificationContent.Gotify,
        probe: ProbeDTO,
        result: ProbeResult,
    ) {
        val title = "Service ${probe.name} is ${result.status}"
        send(content, buildPayload(title, result, PRIORITY_HIGH))
    }

    override fun sendReminder(
        content: NotificationContent.Gotify,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        val title = "Service ${probe.name} is still ${result.status} (reminder #$reminderIndex)"
        send(content, buildPayload(title, result, PRIORITY_HIGH))
    }

    override fun sendTest(content: NotificationContent.Gotify) {
        val result = ProbeResult(ProbeMonitorLogStatus.SUCCESS, 0, "Test notification", Instant.now())
        send(content, buildPayload("Test notification", result, PRIORITY_DEFAULT))
    }

    override fun getNotificationType() = NotificationChannelsEnum.GOTIFY.name

    internal fun buildPayload(
        title: String,
        result: ProbeResult,
        priority: Int,
    ): String {
        val message =
            listOf(result.message, "Status: ${result.status}", "Date: ${dates.format(result.runAt)}")
                .filter { it.isNotBlank() }
                .joinToString("\n")

        return buildString {
            append("{")
            append("\"title\":\"${JsonText.escape(title)}\"")
            append(",\"message\":\"${JsonText.escape(message.take(MAX_MESSAGE_LENGTH))}\"")
            append(",\"priority\":$priority")
            append(",\"extras\":{\"client::display\":{\"contentType\":\"text/plain\"}}")
            append("}")
        }
    }

    private fun send(
        content: NotificationContent.Gotify,
        json: String,
    ) {
        val headers = mapOf("X-Gotify-Key" to encryption.decryptIfEncrypted(content.appToken))
        http.deliver("Gotify", http.postJson(content.serverUrl.trimEnd('/') + "/message", json, headers), ::describe)
    }

    private fun describe(body: String): String = ERROR.find(body)?.groupValues?.get(1) ?: body.take(200)

    private companion object {
        const val MAX_MESSAGE_LENGTH = 3500
        const val PRIORITY_DEFAULT = 5
        const val PRIORITY_HIGH = 8
        val ERROR = Regex("\"errorDescription\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
    }
}
