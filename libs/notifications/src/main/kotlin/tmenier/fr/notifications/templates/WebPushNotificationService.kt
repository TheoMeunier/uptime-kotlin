package tmenier.fr.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.dtos.WebPushSubscription
import tmenier.fr.notifications.JsonText
import tmenier.fr.notifications.NotificationDateFormatter
import tmenier.fr.notifications.NotificationHttpClient
import tmenier.fr.notifications.TypedNotificationInterfaces
import tmenier.fr.notifications.resolvers.OutageWindow
import tmenier.fr.notifications.webpush.ExpiredSubscriptions
import tmenier.fr.notifications.webpush.VapidKeySource
import tmenier.fr.notifications.webpush.WebPushCrypto
import java.net.URI
import java.net.http.HttpRequest
import java.time.Duration
import java.time.Instant

@ApplicationScoped
class WebPushNotificationService(
    private val http: NotificationHttpClient,
    private val dates: NotificationDateFormatter,
    private val encryption: EncryptionService,
    private val vapid: VapidKeySource,
    private val expiredSubscriptions: ExpiredSubscriptions,
    @param:ConfigProperty(name = "notifications.web-push.subject", defaultValue = "https://github.com/TheoMeunier/uptime-kotlin")
    private val subject: String,
) : TypedNotificationInterfaces<NotificationContent.WebPush> {
    override fun sendSuccess(
        content: NotificationContent.WebPush,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        val title = "${probe.name} is ${result.status}${OutageWindow.suffix(downtime)}"
        send(content, buildPayload(title, result, tag = probe.id.toString(), url = "/monitors/${probe.id}", urgent = false), URGENCY_NORMAL)
    }

    override fun sendFailure(
        content: NotificationContent.WebPush,
        probe: ProbeDTO,
        result: ProbeResult,
    ) {
        val title = "${probe.name} is ${result.status}"
        send(content, buildPayload(title, result, tag = probe.id.toString(), url = "/monitors/${probe.id}", urgent = true), URGENCY_HIGH)
    }

    override fun sendReminder(
        content: NotificationContent.WebPush,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        val title = "${probe.name} is still ${result.status} (reminder #$reminderIndex)"
        send(content, buildPayload(title, result, tag = probe.id.toString(), url = "/monitors/${probe.id}", urgent = true), URGENCY_HIGH)
    }

    override fun sendTest(content: NotificationContent.WebPush) {
        val result = ProbeResult(ProbeMonitorLogStatus.SUCCESS, 0, "Web notifications work in this browser.", Instant.now())
        send(content, buildPayload("Test notification", result, tag = "uptime-kotlin-test", url = "/", urgent = false), URGENCY_NORMAL)
    }

    override fun getNotificationType() = NotificationChannelsEnum.WEB_PUSH.name

    internal fun buildPayload(
        title: String,
        result: ProbeResult,
        tag: String,
        url: String,
        urgent: Boolean,
    ): String {
        val message =
            listOf(result.message.take(MAX_MESSAGE_LENGTH), dates.format(result.runAt))
                .filter { it.isNotBlank() }
                .joinToString("\n")

        return buildString {
            append("{")
            append("\"title\":\"${JsonText.escape(title.take(MAX_TITLE_LENGTH))}\"")
            append(",\"body\":\"${JsonText.escape(message)}\"")
            append(",\"status\":\"${result.status}\"")
            append(",\"tag\":\"${JsonText.escape(tag)}\"")
            append(",\"url\":\"${JsonText.escape(url)}\"")
            append(",\"require_interaction\":$urgent")
            append("}")
        }
    }

    private fun send(
        content: NotificationContent.WebPush,
        json: String,
        urgency: String,
    ) {
        check(content.subscriptions.isNotEmpty()) { "No browser is subscribed to this channel: edit it and add a browser" }

        val outcomes = content.subscriptions.map { subscription -> subscription to runCatching { sendTo(subscription, json, urgency) } }
        val delivered = outcomes.count { (_, outcome) -> outcome.getOrNull() == Delivery.DELIVERED }
        val expired = outcomes.filter { (_, outcome) -> outcome.getOrNull() == Delivery.EXPIRED }.map { it.first }
        val failures = outcomes.mapNotNull { (subscription, outcome) -> outcome.exceptionOrNull()?.let { subscription to it } }

        expired.forEach { expiredSubscriptions.forget(it.endpoint) }
        failures.forEach { (subscription, error) ->
            logger.warn { "Web Push to ${describe(subscription)} failed: ${error.message}" }
        }
        logger.info { "Web Push: $delivered delivered, ${expired.size} expired, ${failures.size} failed" }

        if (delivered > 0) return
        failures.firstOrNull()?.let { throw it.second }
        error(
            "Every browser subscription of this channel has expired or was revoked: edit the channel and add the browser again",
        )
    }

    private fun sendTo(
        subscription: WebPushSubscription,
        json: String,
        urgency: String,
    ): Delivery {
        val body =
            WebPushCrypto.encrypt(
                plaintext = json.toByteArray(),
                p256dh = WebPushCrypto.decodeBase64(subscription.p256dh),
                auth = WebPushCrypto.decodeBase64(encryption.decryptIfEncrypted(subscription.auth)),
            )

        val request =
            http
                .request(subscription.endpoint)
                .setHeader("Content-Type", "application/octet-stream")
                .header("Content-Encoding", "aes128gcm")
                .header("TTL", TTL_SECONDS.toString())
                .header("Urgency", urgency)
                .header("Authorization", WebPushCrypto.vapidAuthorization(subscription.endpoint, subject, vapid.current()))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build()

        val response = http.send(request)
        logger.info { "Web Push service answered HTTP ${response.statusCode()} for ${describe(subscription)}" }
        return when (response.statusCode()) {
            in 200..299 -> Delivery.DELIVERED
            404, 410 -> Delivery.EXPIRED
            else -> error("push service returned HTTP ${response.statusCode()}: ${response.body().take(MAX_ERROR_LENGTH)}")
        }
    }

    private fun describe(subscription: WebPushSubscription): String {
        val host = runCatching { URI.create(subscription.endpoint).host }.getOrNull() ?: "unknown push service"
        return subscription.label?.let { "$it ($host)" } ?: host
    }

    private enum class Delivery { DELIVERED, EXPIRED }

    private companion object {
        const val TTL_SECONDS = 24 * 60 * 60
        const val URGENCY_HIGH = "high"
        const val URGENCY_NORMAL = "normal"
        const val MAX_TITLE_LENGTH = 200
        const val MAX_MESSAGE_LENGTH = 600
        const val MAX_ERROR_LENGTH = 300
    }
}
