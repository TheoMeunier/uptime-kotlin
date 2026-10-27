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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class MatrixNotificationService(
    private val http: NotificationHttpClient,
    private val dates: NotificationDateFormatter,
    private val encryption: EncryptionService,
) : TypedNotificationInterfaces<NotificationContent.Matrix> {
    override fun sendSuccess(
        content: NotificationContent.Matrix,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        val title = "Service ${probe.name} is ${result.status}${OutageWindow.suffix(downtime)}"
        send(content, txnId("recovery", probe.id, result.runAt), buildPayload("✅", title, result.message, result.status, result.runAt))
    }

    override fun sendFailure(
        content: NotificationContent.Matrix,
        probe: ProbeDTO,
        result: ProbeResult,
    ) {
        val title = "Service ${probe.name} is ${result.status}"
        send(content, txnId("failure", probe.id, result.runAt), buildPayload("🔴", title, result.message, result.status, result.runAt))
    }

    override fun sendReminder(
        content: NotificationContent.Matrix,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        val title = "Service ${probe.name} is still ${result.status} (reminder #$reminderIndex)"
        send(
            content,
            txnId("reminder", probe.id, result.runAt, reminderIndex),
            buildPayload("🟠", title, result.message, result.status, result.runAt),
        )
    }

    override fun sendTest(content: NotificationContent.Matrix) {
        val now = Instant.now()
        send(
            content,
            "uptime-kotlin.test.${UUID.randomUUID()}",
            buildPayload("🔵", "Test notification", "Test notification", ProbeMonitorLogStatus.SUCCESS, now),
        )
    }

    override fun getNotificationType() = NotificationChannelsEnum.MATRIX.name

    internal fun txnId(
        event: String,
        probeId: UUID,
        runAt: Instant,
        reminderIndex: Int = 0,
    ): String = "uptime-kotlin.$event.$probeId.${runAt.toEpochMilli()}.$reminderIndex"

    internal fun sendUrl(
        content: NotificationContent.Matrix,
        txnId: String,
    ): String =
        content.homeserverUrl.trim().trimEnd('/') +
            "/_matrix/client/v3/rooms/${encodePath(content.roomId.trim())}/send/m.room.message/${encodePath(txnId)}"

    internal fun buildPayload(
        emoji: String,
        title: String,
        message: String,
        status: ProbeMonitorLogStatus,
        runAt: Instant,
    ): String {
        val detail = message.take(MAX_DETAIL_LENGTH)
        val date = dates.format(runAt)

        val plain =
            buildString {
                append("$emoji $title")
                if (detail.isNotBlank()) append("\n\n$detail")
                append("\n\nStatus: $status")
                append("\nDate: $date")
            }

        val html =
            buildString {
                append("$emoji <strong>${escapeHtml(title)}</strong>")
                if (detail.isNotBlank()) append("<br><br>${escapeHtml(detail).replace("\n", "<br>")}")
                append("<br><br><strong>Status:</strong> $status")
                append("<br><strong>Date:</strong> ${escapeHtml(date)}")
            }

        return buildString {
            append("{")
            append("\"msgtype\":\"m.text\"")
            append(",\"body\":\"${JsonText.escape(plain)}\"")
            append(",\"format\":\"org.matrix.custom.html\"")
            append(",\"formatted_body\":\"${JsonText.escape(html)}\"")
            append("}")
        }
    }

    private fun send(
        content: NotificationContent.Matrix,
        txnId: String,
        json: String,
    ) {
        val headers = mapOf("Authorization" to "Bearer ${encryption.decryptIfEncrypted(content.accessToken)}")
        http.deliver("Matrix", http.putJson(sendUrl(content, txnId), json, headers), ::describe)
    }

    private fun describe(body: String): String {
        val code = ERRCODE.find(body)?.groupValues?.get(1)
        val error = ERROR.find(body)?.groupValues?.get(1)
        return when {
            code != null && error != null -> "$code: $error"
            else -> error ?: code ?: body.take(200)
        }
    }

    private fun encodePath(segment: String): String = URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20")

    private fun escapeHtml(text: String): String =
        text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private companion object {
        const val MAX_DETAIL_LENGTH = 3000
        val ERRCODE = Regex("\"errcode\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
        val ERROR = Regex("\"error\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
    }
}
