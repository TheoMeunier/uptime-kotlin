package tmenier.fr.monitors.actions.notifications

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.monitors.dtos.requests.BaseStoreNotificationRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelDiscordRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelMailRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelTeamsRequest
import tmenier.fr.monitors.entities.NotificationsChannelEntity
import tmenier.fr.monitors.entities.mapper.NotificationContentMapper
import tmenier.fr.monitors.notifications.dto.NotificationContent
import java.util.*

@ApplicationScoped
class StoreNotificationAction(
    private val encryptionService: EncryptionService,
) {
    fun execute(payload: BaseStoreNotificationRequest) {
        val notification = NotificationsChannelEntity()
        notification.id = UUID.randomUUID()
        notification.name = payload.name
        notification.type = payload.notificationType
        notification.isDefault = payload.isDefault ?: false

        when (payload) {
            is ValidNotificationChannelDiscordRequest -> {
                val (jsonNode, _) =
                    NotificationContentMapper.toEntity(
                        NotificationContent.Discord(
                            webhookUrl = payload.urlWebhook,
                            username = payload.nameReboot,
                        ),
                    )

                notification.content = jsonNode
            }

            is ValidNotificationChannelTeamsRequest -> {
                val (jsonNode, _) =
                    NotificationContentMapper.toEntity(
                        NotificationContent.Teams(
                            webhookUrl = payload.urlWebhook,
                            username = payload.nameReboot,
                        ),
                    )

                notification.content = jsonNode
            }

            is ValidNotificationChannelMailRequest -> {
                val (jsonNode, _) =
                    NotificationContentMapper.toEntity(
                        NotificationContent.Mail(
                            hostname = payload.hostname,
                            port = payload.port,
                            starttls = payload.starttls ?: false,
                            username = payload.username,
                            password = encryptionService.encrypt(payload.password),
                            from = payload.mailFrom,
                            to = payload.mailTo,
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
}
