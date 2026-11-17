package tmenier.fr.backup.sections

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.backup.restore.RestoreContext
import tmenier.fr.backup.services.RequestReader
import tmenier.fr.backup.support.JsonTrees
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.databases.entities.NotificationsChannelEntity
import tmenier.fr.databases.repositories.NotificationRepository
import tmenier.fr.notifications.actions.StoreNotificationAction
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import tmenier.fr.notifications.requests.OnCreate
import tmenier.fr.notifications.services.ResolveNotificationContentService
import java.util.UUID

@ApplicationScoped
class NotificationBackupSection(
    override val requestReader: RequestReader,
    private val notificationRepository: NotificationRepository,
    private val storeNotificationAction: StoreNotificationAction,
    private val contentService: ResolveNotificationContentService,
    private val objectMapper: ObjectMapper,
) : RequestBackupSection<BaseStoreNotificationRequest>() {
    override val kind = BackupItemKind.NOTIFICATION
    override val requestType = BaseStoreNotificationRequest::class.java
    override val validationGroups: Array<Class<*>> = arrayOf(OnCreate::class.java)

    override fun backup(): List<ObjectNode> = notificationRepository.getAll().sortedBy { it.name }.map(::toItem)

    override fun existingKeys(): Map<String, UUID> = notificationRepository.getAll().associate { it.name to it.id }

    override fun rules(
        request: BaseStoreNotificationRequest,
        context: RestoreContext,
    ): List<String> = listOfNotNull(violationOf { contentService.resolve(request, isUpdate = false, existingNotification = null) })

    override fun store(request: BaseStoreNotificationRequest): UUID = storeNotificationAction.execute(request)

    private fun toItem(channel: NotificationsChannelEntity): ObjectNode {
        val item = JsonTrees.factory.objectNode()
        item.put(BackupSection.REF, channel.id.toString())
        item.put("name", channel.name)
        item.set<JsonNode>("notification_type", objectMapper.valueToTree(channel.type))
        item.put("is_default", channel.isDefault)

        (channel.content?.let(JsonTrees::toSnakeCase) as? ObjectNode)?.let { JsonTrees.mergeMissing(item, it) }
        return item
    }
}
