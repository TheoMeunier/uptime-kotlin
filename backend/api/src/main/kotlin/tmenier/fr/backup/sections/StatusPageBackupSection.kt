package tmenier.fr.backup.sections

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.backup.restore.RestoreContext
import tmenier.fr.backup.services.RequestReader
import tmenier.fr.backup.support.JsonTrees
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.databases.entities.StatusPageEntity
import tmenier.fr.databases.entities.StatusPageGroupEntity
import tmenier.fr.databases.repositories.StatusPageRepository
import tmenier.fr.statuspages.actions.StoreStatusPageAction
import tmenier.fr.statuspages.requests.StoreStatusPageRequest
import java.util.UUID

@ApplicationScoped
class StatusPageBackupSection(
    override val requestReader: RequestReader,
    private val statusPageRepository: StatusPageRepository,
    private val storeStatusPageAction: StoreStatusPageAction,
) : RequestBackupSection<StoreStatusPageRequest>() {
    override val kind = BackupItemKind.STATUS_PAGE
    override val requestType = StoreStatusPageRequest::class.java
    override val references = listOf(ReferenceField("probes", "probe_ids", BackupItemKind.PROBE, parentArray = "groups"))

    override fun backup(): List<ObjectNode> = statusPageRepository.getAll().map(::toItem)

    override fun existingKeys(): Map<String, UUID> = statusPageRepository.getAll().associate { it.slug to it.id }

    override fun rules(
        request: StoreStatusPageRequest,
        context: RestoreContext,
    ): List<String> {
        val probeIds = request.groups.flatMap { it.probeIds }
        return listOfNotNull(DUPLICATE_PROBE.takeIf { probeIds.size != probeIds.toSet().size })
    }

    override fun store(request: StoreStatusPageRequest): UUID = storeStatusPageAction.execute(request)

    private fun toItem(page: StatusPageEntity): ObjectNode =
        JsonTrees.factory.objectNode().apply {
            put(BackupSection.REF, page.id.toString())
            put("slug", page.slug)
            put("title", page.title)
            put("description", page.description)
            put("default_layout", page.defaultLayout.name)
            put("uptime_period", page.uptimePeriod.name)
            put("is_private", page.isPrivate)
            set<JsonNode>("groups", JsonTrees.factory.arrayNode().apply { page.groups.forEach { add(toItem(it)) } })
        }

    private fun toItem(group: StatusPageGroupEntity): ObjectNode =
        JsonTrees.factory.objectNode().apply {
            put("name", group.name)
            set<JsonNode>("probes", JsonTrees.array(group.orderedProbes().map { it.id.toString() }))
        }

    private companion object {
        const val DUPLICATE_PROBE = "A monitor can only appear once on a status page"
    }
}
