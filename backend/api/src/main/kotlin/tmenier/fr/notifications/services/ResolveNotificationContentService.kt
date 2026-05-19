package tmenier.fr.notifications.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.NotificationDto
import tmenier.fr.databases.mappers.NotificationContentMapper
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelDiscordRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelMailRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelSlackRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelTeamsRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelWebhookRequest

@ApplicationScoped
class ResolveNotificationContentService(
    private val encryptionService: EncryptionService,
    private val objectMapper: ObjectMapper,
) {
    fun resolveForTesting(request: BaseStoreNotificationRequest): NotificationContent =
        resolve(request, isUpdate = false, existingNotification = null, isTesting = true)

    fun resolve(
        request: BaseStoreNotificationRequest,
        isUpdate: Boolean,
        existingNotification: NotificationDto?,
        isTesting: Boolean = false,
    ): NotificationContent =
        when (request) {
            is ValidNotificationChannelDiscordRequest ->
                NotificationContent.Discord(
                    webhookUrl = request.webhookUrl,
                    username = request.username,
                )

            is ValidNotificationChannelSlackRequest ->
                NotificationContent.Slack(
                    webhookUrl = request.webhookUrl,
                    username = request.username,
                )

            is ValidNotificationChannelTeamsRequest ->
                NotificationContent.Teams(
                    webhookUrl = request.webhookUrl,
                    username = request.username,
                )

            is ValidNotificationChannelWebhookRequest ->
                NotificationContent.Webhook(
                    url = request.url,
                    method = request.method,
                )

            is ValidNotificationChannelMailRequest ->
                NotificationContent.Mail(
                    hostname = request.hostname,
                    port = request.port,
                    starttls = request.starttls ?: false,
                    username = request.username,
                    password =
                        if (isTesting) {
                            requireNotNull(request.password) { "Password is required for testing" }
                        } else {
                            resolvePassword(request.password, isUpdate, existingNotification)
                        },
                    from = request.from,
                    to = request.to,
                )

            else -> throw IllegalArgumentException("Invalid notification channel type: ${request.notificationType}")
        }

    private fun resolvePassword(
        incomingPassword: String?,
        isUpdate: Boolean,
        existingNotification: NotificationDto?,
    ): String {
        if (!isUpdate) {
            requireNotNull(incomingPassword) { "Password is required on creation" }
            return encryptionService.encrypt(incomingPassword)
        }

        return if (incomingPassword != null) {
            encryptionService.encrypt(incomingPassword)
        } else {
            val content =
                requireNotNull(existingNotification) {
                    "Existing notification not found for update"
                }.content

            val contentAsJsonNode = objectMapper.valueToTree<JsonNode>(content)

            NotificationContentMapper.extractPassword(contentAsJsonNode)
                ?: error("Existing notification has no password to preserve")
        }
    }
}
