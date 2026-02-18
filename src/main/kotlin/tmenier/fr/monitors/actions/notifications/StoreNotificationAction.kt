package tmenier.fr.monitors.actions.notifications

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.monitors.dtos.requests.BaseStoreNotificationRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelDiscordRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelMailRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelSlackRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelTeamsRequest
import tmenier.fr.monitors.entities.NotificationsChannelEntity
import tmenier.fr.monitors.entities.mapper.NotificationContentMapper
import tmenier.fr.monitors.notifications.dto.NotificationContent
import java.util.UUID

@ApplicationScoped
class StoreNotificationAction(
    private val encryptionService: EncryptionService,
) {
    @Transactional
    fun execute(
        payload: BaseStoreNotificationRequest,
        notificationId: UUID? = null,
    ) {
        val isUpdate = notificationId != null

        val notification =
            if (isUpdate) {
                NotificationsChannelEntity.findById(notificationId!!)
                    ?: throw NotFoundException("Notification with id $notificationId not found")
            } else {
                NotificationsChannelEntity().apply { id = UUID.randomUUID() }
            }

        notification.name = payload.name
        notification.type = payload.notificationType
        notification.isDefault = payload.isDefault ?: false

        when (payload) {
            is ValidNotificationChannelDiscordRequest -> {
                val (jsonNode, _) =
                    NotificationContentMapper.toEntity(
                        NotificationContent.Discord(
                            webhookUrl = payload.webhookUrl,
                            username = payload.username,
                        ),
                    )

                notification.content = jsonNode
            }

            is ValidNotificationChannelTeamsRequest -> {
                val (jsonNode, _) =
                    NotificationContentMapper.toEntity(
                        NotificationContent.Teams(
                            webhookUrl = payload.webhookUrl,
                            username = payload.username,
                        ),
                    )

                notification.content = jsonNode
            }

            is ValidNotificationChannelSlackRequest -> {
                val (jsonNode, _) =
                    NotificationContentMapper.toEntity(
                        NotificationContent.Slack(
                            webhookUrl = payload.webhookUrl,
                            username = payload.username,
                        ),
                    )

                notification.content = jsonNode
            }

            is ValidNotificationChannelMailRequest -> {
                val resolvedPassword =
                    resolvePassword(
                        isUpdate = isUpdate,
                        incomingPassword = payload.password,
                        existingNotification = notification,
                    )

                val (jsonNode, _) =
                    NotificationContentMapper.toEntity(
                        NotificationContent.Mail(
                            hostname = payload.hostname,
                            port = payload.port,
                            starttls = payload.starttls ?: false,
                            username = payload.username,
                            password = resolvedPassword,
                            from = payload.from,
                            to = payload.to,
                        ),
                    )

                notification.content = jsonNode
            }

            else -> {
                throw IllegalArgumentException("Invalid notification channel type: ${payload.notificationType}")
            }
        }

        notification.persist()
    }

    private fun resolvePassword(
        isUpdate: Boolean,
        incomingPassword: String?,
        existingNotification: NotificationsChannelEntity,
    ): String {
        if (!isUpdate) {
            requireNotNull(incomingPassword) { "Password is required on creation" }
            return encryptionService.encrypt(incomingPassword)
        }

        return if (incomingPassword != null) {
            encryptionService.encrypt(incomingPassword)
        } else {
            val content = existingNotification.content ?: error("Existing notification has no content to preserve")

            NotificationContentMapper.extractPassword(content)
                ?: error("Existing notification has no password to preserve")
        }
    }
}
