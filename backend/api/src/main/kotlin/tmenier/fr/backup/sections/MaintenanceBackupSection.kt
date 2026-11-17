package tmenier.fr.backup.sections

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.backup.restore.RestoreContext
import tmenier.fr.backup.services.RequestReader
import tmenier.fr.backup.support.JsonTrees
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.databases.entities.MaintenanceWindowEntity
import tmenier.fr.databases.repositories.MaintenanceWindowRepository
import tmenier.fr.maintenances.actions.StoreMaintenanceAction
import tmenier.fr.maintenances.requests.StoreMaintenanceRequest
import java.util.UUID

@ApplicationScoped
class MaintenanceBackupSection(
    override val requestReader: RequestReader,
    private val maintenanceWindowRepository: MaintenanceWindowRepository,
    private val storeMaintenanceAction: StoreMaintenanceAction,
) : RequestBackupSection<StoreMaintenanceRequest>() {
    override val kind = BackupItemKind.MAINTENANCE
    override val requestType = StoreMaintenanceRequest::class.java
    override val references = listOf(ReferenceField("probes", "probe_ids", BackupItemKind.PROBE))

    override fun backup(): List<ObjectNode> = maintenanceWindowRepository.getAll().sortedBy { it.startsAt }.map(::toItem)

    override fun existingKeys(): Map<String, UUID> = maintenanceWindowRepository.getAll().associate { it.title to it.id }

    override fun rules(
        request: StoreMaintenanceRequest,
        context: RestoreContext,
    ): List<String> = listOfNotNull(violationOf { storeMaintenanceAction.validate(request) })

    override fun store(request: StoreMaintenanceRequest): UUID = storeMaintenanceAction.execute(request)

    private fun toItem(window: MaintenanceWindowEntity): ObjectNode =
        JsonTrees.factory.objectNode().apply {
            put(BackupSection.REF, window.id.toString())
            put("title", window.title)
            put("description", window.description)
            put("starts_at", window.startsAt.toString())
            put("duration_seconds", window.durationSeconds)
            put("recurrence", window.recurrence.name)
            put("recurrence_until", window.recurrenceUntil?.toString())
            put("timezone", window.timezone)
            put("active", window.active)
            put("is_public", window.isPublic)
            set<JsonNode>("probes", JsonTrees.array(window.probes.map { it.id.toString() }.sorted()))
        }
}
