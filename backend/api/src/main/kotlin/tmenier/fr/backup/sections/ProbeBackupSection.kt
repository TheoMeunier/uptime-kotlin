package tmenier.fr.backup.sections

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.backup.dtos.RestoreIssue
import tmenier.fr.backup.restore.RestoreContext
import tmenier.fr.backup.services.RequestReader
import tmenier.fr.backup.support.JsonTrees
import tmenier.fr.backup.support.JsonTrees.isAbsent
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.common.enums.backup.RestoreIssueCode
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.ProbeContentMapper
import tmenier.fr.databases.repositories.NotificationRepository
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.monitors.actions.StoreProbeAction
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.requests.OnCreate
import tmenier.fr.monitors.services.ResolveMonitorContentService
import java.util.UUID

@ApplicationScoped
class ProbeBackupSection(
    override val requestReader: RequestReader,
    private val probeRepository: ProbeRepository,
    private val notificationRepository: NotificationRepository,
    private val storeProbeAction: StoreProbeAction,
    private val contentService: ResolveMonitorContentService,
    private val objectMapper: ObjectMapper,
) : RequestBackupSection<BaseStoreProbeRequest>() {
    override val kind = BackupItemKind.PROBE
    override val requestType = BaseStoreProbeRequest::class.java
    override val validationGroups: Array<Class<*>> = arrayOf(OnCreate::class.java)
    override val references = listOf(ReferenceField("notifications", "notifications", BackupItemKind.NOTIFICATION))

    override fun backup(): List<ObjectNode> = probeRepository.getAll().map(::toItem)

    override fun existingKeys(): Map<String, UUID> = probeRepository.getAll().associate { it.name to it.id }

    override fun rules(
        request: BaseStoreProbeRequest,
        context: RestoreContext,
    ): List<String> =
        listOfNotNull(
            NOTIFICATION_REQUIRED.takeIf { request.notifications.isEmpty() && !hasDefaultNotification(context) },
            violationOf { contentService.resolve(request) },
        )

    override fun notes(request: BaseStoreProbeRequest): List<RestoreIssue> =
        if (request.protocol == ProbeProtocol.PUSH) listOf(RestoreIssue(RestoreIssueCode.PUSH_URL_REGENERATED)) else emptyList()

    override fun store(request: BaseStoreProbeRequest): UUID = storeProbeAction.execute(request)

    private fun hasDefaultNotification(context: RestoreContext): Boolean =
        notificationRepository.hasDefault() ||
            context.accepted(BackupItemKind.NOTIFICATION).any { it.path("is_default").asBoolean(false) }

    private fun toItem(probe: ProbesEntity): ObjectNode {
        val item = JsonTrees.factory.objectNode()
        item.put(BackupSection.REF, probe.id.toString())
        item.put("name", probe.name)
        item.set<JsonNode>("protocol", objectMapper.valueToTree(probe.protocol))
        probe.description?.takeIf(String::isNotBlank)?.let { item.put("description", it) }
        item.put("enabled", probe.enabled)
        item.put("timeout", probe.timeout)
        if (probe.protocol != ProbeProtocol.PUSH) {
            item.put("interval", probe.interval)
            item.put("interval_retry", probe.intervalRetry)
            item.put("retry", probe.retry)
        }
        item.put("alert_repeat_seconds", probe.alertRepeatSeconds)
        item.put("log_retention_days", probe.logRetentionDays)
        item.set<JsonNode>("notifications", JsonTrees.array(probe.notifications.map { it.id.toString() }.sorted()))

        JsonTrees.mergeMissing(item, requestContent(probe))
        return item
    }

    private fun requestContent(probe: ProbesEntity): ObjectNode {
        val content =
            runCatching { objectMapper.valueToTree<ObjectNode>(ProbeContentMapper.toDto(probe)) }
                .getOrElse { JsonTrees.toSnakeCase(probe.content ?: JsonTrees.factory.objectNode()) as ObjectNode }

        DERIVED_FIELDS.forEach { content.remove(it) }
        RENAMED_FIELDS.forEach { (stored, requested) -> content.remove(stored)?.let { content.set<JsonNode>(requested, it) } }
        DEFAULTS[probe.protocol].orEmpty().forEach { (field, value) ->
            if (content.isAbsent(field)) content.set<JsonNode>(field, objectMapper.valueToTree(value))
        }
        return content
    }

    private companion object {
        const val NOTIFICATION_REQUIRED = "At least one notification is required: link a channel, or mark one as default"

        val DERIVED_FIELDS = listOf("host")

        val RENAMED_FIELDS = mapOf("notification_certified" to "notification_certificate")

        val DEFAULTS: Map<ProbeProtocol, Map<String, Any>> =
            mapOf(
                ProbeProtocol.PING to mapOf("ping_heartbeat_interval" to 1, "ping_numeric_output" to false),
                ProbeProtocol.DNS to mapOf("record_type" to "A"),
            )
    }
}
