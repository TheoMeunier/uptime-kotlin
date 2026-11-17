package tmenier.fr.backup.sections

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.announcements.actions.StoreStatusAnnouncementAction
import tmenier.fr.announcements.requests.StoreStatusAnnouncementRequest
import tmenier.fr.backup.restore.RestoreContext
import tmenier.fr.backup.services.RequestReader
import tmenier.fr.backup.support.JsonTrees
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.databases.entities.StatusAnnouncementEntity
import tmenier.fr.databases.repositories.StatusAnnouncementRepository
import java.util.UUID

@ApplicationScoped
class AnnouncementBackupSection(
    override val requestReader: RequestReader,
    private val announcementRepository: StatusAnnouncementRepository,
    private val storeAnnouncementAction: StoreStatusAnnouncementAction,
) : RequestBackupSection<StoreStatusAnnouncementRequest>() {
    override val kind = BackupItemKind.ANNOUNCEMENT
    override val requestType = StoreStatusAnnouncementRequest::class.java
    override val references = listOf(ReferenceField("status_pages", "status_page_ids", BackupItemKind.STATUS_PAGE))

    override fun backup(): List<ObjectNode> = announcementRepository.getAll().sortedBy { it.startsAt }.map(::toItem)

    override fun existingKeys(): Map<String, UUID> = announcementRepository.getAll().associate { it.title to it.id }

    override fun rules(
        request: StoreStatusAnnouncementRequest,
        context: RestoreContext,
    ): List<String> {
        val startsAt = request.startsAt ?: context.now
        return listOfNotNull(
            END_BEFORE_START.takeIf { request.endsAt?.isAfter(startsAt) == false },
            NO_TARGET.takeIf { !request.showOnGlobal && request.statusPageIds.isEmpty() },
        )
    }

    override fun store(request: StoreStatusAnnouncementRequest): UUID = storeAnnouncementAction.execute(request)

    private fun toItem(announcement: StatusAnnouncementEntity): ObjectNode =
        JsonTrees.factory.objectNode().apply {
            put(BackupSection.REF, announcement.id.toString())
            put("title", announcement.title)
            put("body", announcement.body)
            put("severity", announcement.severity.name)
            put("starts_at", announcement.startsAt.toString())
            put("ends_at", announcement.endsAt?.toString())
            put("show_on_global", announcement.showOnGlobal)
            set<JsonNode>("status_pages", JsonTrees.array(announcement.pages.map { it.id.toString() }.sorted()))
        }

    private companion object {
        const val END_BEFORE_START = "The end of an announcement must come after its start"
        const val NO_TARGET = "Choose at least one status page for the announcement"
    }
}
