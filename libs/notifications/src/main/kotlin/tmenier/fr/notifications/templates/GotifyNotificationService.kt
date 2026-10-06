package tmenier.fr.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.notifications.NotificationHttpClient
import tmenier.fr.notifications.resolvers.OutageWindow
import java.net.http.HttpRequest
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** Pushes to a Gotify server: POST {serverUrl}/message, authenticated by an application token. */
@ApplicationScoped
class GotifyNotificationService(
    private val http: NotificationHttpClient,
    private val encryptionService: EncryptionService,
) : tmenier.fr.notifications.TypedNotificationInterfaces<NotificationContent.Gotify> {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC)

    override fun sendSuccess(
        content: NotificationContent.Gotify,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        sendGotifyMessage(
            content,
            buildPayload(
                title = "✅ Service ${probe.name} - ${result.status}${OutageWindow.suffix(downtime)}",
                message = result.message,
                runAt = result.runAt,
                priority = content.priority,
            ),
        )
    }

    override fun sendFailure(
        content: NotificationContent.Gotify,
        probe: ProbeDTO,
        result: ProbeResult,
    ) {
        sendGotifyMessage(
            content,
            buildPayload(
                title = "❌ Service ${probe.name} - ${result.status}",
                message = result.message,
                runAt = result.runAt,
                priority = content.priority,
            ),
        )
    }

    override fun sendReminder(
        content: NotificationContent.Gotify,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        sendGotifyMessage(
            content,
            buildPayload(
                title = "⚠️ Service ${probe.name} - still ${result.status} (reminder #$reminderIndex)",
                message = result.message,
                runAt = result.runAt,
                priority = content.priority,
            ),
        )
    }

    override fun sendTest(content: NotificationContent.Gotify) {
        sendGotifyMessage(
            content,
            buildPayload(
                title = "Test notification",
                message = "Test notification from Uptime Kotlin",
                runAt = Instant.now(),
                priority = content.priority,
            ),
        )
    }

    override fun getNotificationType() = NotificationChannelsEnum.GOTIFY.name

    private fun buildPayload(
        title: String,
        message: String,
        runAt: Instant,
        priority: Int,
    ): String =
        """
        {
            "title": "${escapeJson(title)}",
            "message": "${escapeJson("$message\n\n${dateFormatter.format(runAt)}")}",
            "priority": $priority
        }
        """.trimIndent()

    private fun messageUrl(serverUrl: String): String = "${serverUrl.trimEnd('/')}/message"

    private fun sendGotifyMessage(
        content: NotificationContent.Gotify,
        jsonPayload: String,
    ) {
        try {
            val token =
                encryptionService.decrypt(
                    requireNotNull(content.token) { "Gotify application token is missing" },
                )
            val request =
                http
                    .request(messageUrl(content.serverUrl))
                    .header("Content-Type", "application/json")
                    .header("X-Gotify-Key", token)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build()

            val response = http.send(request)

            logger.info { "Gotify API response: ${response.statusCode()}" }

            if (response.statusCode() !in 200..299) {
                throw IllegalStateException("Gotify returned HTTP ${response.statusCode()}: ${response.body()}")
            }
            logger.info { "Gotify notification sent successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Exception while sending Gotify notification: ${e.message}" }
            throw e
        }
    }

    // Every control character must be escaped: a raw one makes Gotify reject the message with HTTP 400.
    private fun escapeJson(text: String): String =
        buildString(text.length) {
            for (char in text) {
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> if (char < ' ') append("\\u%04x".format(char.code)) else append(char)
                }
            }
        }
}
