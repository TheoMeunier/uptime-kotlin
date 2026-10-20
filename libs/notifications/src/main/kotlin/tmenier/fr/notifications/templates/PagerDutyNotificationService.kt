package tmenier.fr.notifications.templates

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.notifications.JsonText
import tmenier.fr.notifications.NotificationHttpClient
import tmenier.fr.notifications.TypedNotificationInterfaces
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class PagerDutyNotificationService(
    private val http: NotificationHttpClient,
    private val encryption: EncryptionService,
    @param:ConfigProperty(name = "notifications.pagerduty.events-url", defaultValue = "https://events.pagerduty.com/v2/enqueue")
    private val eventsUrl: String,
) : TypedNotificationInterfaces<NotificationContent.PagerDuty> {
    override fun sendSuccess(
        content: NotificationContent.PagerDuty,
        probe: ProbeDTO,
        result: ProbeResult,
        downtime: Duration?,
    ) {
        send(content, buildResolve(content, dedupKey(probe)))
    }

    override fun sendFailure(
        content: NotificationContent.PagerDuty,
        probe: ProbeDTO,
        result: ProbeResult,
    ) {
        val summary = "Service ${probe.name} is ${result.status}"
        send(content, buildTrigger(content, dedupKey(probe), summary, probe, result, severityOf(content)))
    }

    override fun sendReminder(
        content: NotificationContent.PagerDuty,
        probe: ProbeDTO,
        result: ProbeResult,
        reminderIndex: Int,
    ) {
        val summary = "Service ${probe.name} is still ${result.status} (reminder #$reminderIndex)"
        send(content, buildTrigger(content, dedupKey(probe), summary, probe, result, severityOf(content)))
    }

    /** Opens an `info` alert and resolves it right away: PagerDuty has no dry run to check a routing key. */
    override fun sendTest(content: NotificationContent.PagerDuty) {
        val key = "$DEDUP_PREFIX:test:${UUID.randomUUID()}"
        send(content, buildTrigger(content, key, "Uptime Kotlin test notification", probe = null, result = null, severity = SEVERITY_INFO))
        send(content, buildResolve(content, key))
    }

    override fun getNotificationType() = NotificationChannelsEnum.PAGERDUTY.name

    internal fun dedupKey(probe: ProbeDTO) = "$DEDUP_PREFIX:probe:${probe.id}"

    internal fun buildTrigger(
        content: NotificationContent.PagerDuty,
        dedupKey: String,
        summary: String,
        probe: ProbeDTO?,
        result: ProbeResult?,
        severity: String,
    ): String {
        val fullSummary =
            listOfNotNull(summary, result?.message?.takeIf { it.isNotBlank() })
                .joinToString(": ")
                .take(MAX_SUMMARY_LENGTH)
        val source = probe?.url?.takeIf { it.isNotBlank() } ?: probe?.name ?: CLIENT
        val timestamp = result?.runAt ?: Instant.now()

        val details =
            buildList {
                probe?.let { add("\"probe_id\":\"${it.id}\"") }
                result?.let {
                    add("\"status\":\"${it.status.name}\"")
                    add("\"message\":\"${JsonText.escape(it.message.take(MAX_DETAIL_LENGTH))}\"")
                    add("\"response_time_ms\":${it.responseTime}")
                    it.statusCode?.let { code -> add("\"status_code\":$code") }
                }
            }.joinToString(",")

        return buildString {
            append("{")
            append("\"routing_key\":\"${JsonText.escape(routingKey(content))}\"")
            append(",\"event_action\":\"trigger\"")
            append(",\"dedup_key\":\"${JsonText.escape(dedupKey)}\"")
            append(",\"client\":\"$CLIENT\"")
            append(",\"payload\":{")
            append("\"summary\":\"${JsonText.escape(fullSummary)}\"")
            append(",\"source\":\"${JsonText.escape(source.take(MAX_SOURCE_LENGTH))}\"")
            append(",\"severity\":\"$severity\"")
            append(",\"timestamp\":\"$timestamp\"")
            probe?.let { append(",\"component\":\"${JsonText.escape(it.name)}\"") }
            append(",\"custom_details\":{$details}")
            append("}}")
        }
    }

    internal fun buildResolve(
        content: NotificationContent.PagerDuty,
        dedupKey: String,
    ): String {
        val key = JsonText.escape(routingKey(content))
        return "{\"routing_key\":\"$key\",\"event_action\":\"resolve\",\"dedup_key\":\"${JsonText.escape(dedupKey)}\"}"
    }

    private fun routingKey(content: NotificationContent.PagerDuty) = encryption.decryptIfEncrypted(content.routingKey)

    private fun severityOf(content: NotificationContent.PagerDuty) = content.severity?.lowercase()?.takeIf { it in SEVERITIES } ?: SEVERITY_CRITICAL

    private fun send(
        content: NotificationContent.PagerDuty,
        json: String,
    ) {
        http.deliver("PagerDuty", http.postJson(eventsUrl, json)) { body -> describe(body, routingKey(content)) }
    }

    /** `{"status":"invalid event","message":"Event object is invalid","errors":["..."]}` → "Event object is invalid (...)". */
    internal fun describe(
        body: String,
        routingKey: String,
    ): String {
        val message = MESSAGE.find(body)?.groupValues?.get(1)
        val firstError = ERRORS.find(body)?.groupValues?.get(1)
        val text =
            when {
                message != null && firstError != null -> "$message ($firstError)"
                message != null -> message
                else -> body.take(MAX_ERROR_LENGTH)
            }
        return if (routingKey.isBlank()) text else text.replace(routingKey, "***")
    }

    companion object {
        const val SEVERITY_CRITICAL = "critical"
        const val SEVERITY_INFO = "info"
        val SEVERITIES = setOf("critical", "error", "warning", "info")

        private const val CLIENT = "Uptime Kotlin"
        private const val DEDUP_PREFIX = "uptime-kotlin"
        private const val MAX_SUMMARY_LENGTH = 1024
        private const val MAX_SOURCE_LENGTH = 255
        private const val MAX_DETAIL_LENGTH = 2000
        private const val MAX_ERROR_LENGTH = 200
        private val MESSAGE = Regex("\"message\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
        private val ERRORS = Regex("\"errors\"\\s*:\\s*\\[\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
    }
}
