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
class NtfyNotificationService(
    private val http: NotificationHttpClient,
    private val dates: NotificationDateFormatter,
    private val encryption: EncryptionService,
) : TypedNotificationInterfaces<NotificationContent.Ntfy> {
    override fun sendSuccess(
        content: NotificationContent.Ntfy,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        val title = "Service ${probe.name} is ${result.status}${OutageWindow.suffix(downtime)}"
        send(content, buildPayload(content.topic, title, result, PRIORITY_DEFAULT, "white_check_mark"))
    }

    override fun sendFailure(
        content: NotificationContent.Ntfy,
        probe: ProbeDTO,
        result: ProbeResult,
    ) {
        val title = "Service ${probe.name} is ${result.status}"
        send(content, buildPayload(content.topic, title, result, PRIORITY_HIGH, "rotating_light"))
    }

    override fun sendReminder(
        content: NotificationContent.Ntfy,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        val title = "Service ${probe.name} is still ${result.status} (reminder #$reminderIndex)"
        send(content, buildPayload(content.topic, title, result, PRIORITY_HIGH, "warning"))
    }

    override fun sendTest(content: NotificationContent.Ntfy) {
        val result = ProbeResult(ProbeMonitorLogStatus.SUCCESS, 0, "Test notification", Instant.now())
        send(content, buildPayload(content.topic, "Test notification", result, PRIORITY_DEFAULT, "information_source"))
    }

    override fun getNotificationType() = NotificationChannelsEnum.NTFY.name

    internal fun buildPayload(
        topic: String,
        title: String,
        result: ProbeResult,
        priority: Int,
        tag: String,
    ): String {
        val message =
            listOf(result.message, "Status: ${result.status}", "Date: ${dates.format(result.runAt)}")
                .filter { it.isNotBlank() }
                .joinToString("\n")

        return buildString {
            append("{")
            append("\"topic\":\"${JsonText.escape(topic)}\"")
            append(",\"title\":\"${JsonText.escape(title)}\"")
            append(",\"message\":\"${JsonText.escape(message.take(MAX_MESSAGE_LENGTH))}\"")
            append(",\"priority\":$priority")
            append(",\"tags\":[\"$tag\"]")
            append("}")
        }
    }

    private fun send(
        content: NotificationContent.Ntfy,
        json: String,
    ) {
        val headers =
            content.accessToken
                ?.let { mapOf("Authorization" to "Bearer ${encryption.decryptIfEncrypted(it)}") }
                ?: emptyMap()
        http.deliver("ntfy", http.postJson(content.serverUrl.trimEnd('/') + "/", json, headers), ::describe)
    }

    private fun describe(body: String): String = ERROR.find(body)?.groupValues?.get(1) ?: body.take(200)

    private companion object {
        const val MAX_MESSAGE_LENGTH = 3500
        const val PRIORITY_DEFAULT = 3
        const val PRIORITY_HIGH = 4
        val ERROR = Regex("\"error\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
    }
}
