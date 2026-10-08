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
import tmenier.fr.notifications.requests.ValidNotificationChannelGotifyRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelMailRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelNtfyRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelSlackRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelTeamsRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelTelegramRequest
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

            is ValidNotificationChannelTelegramRequest ->
                NotificationContent.Telegram(
                    botToken =
                        requireNotNull(resolveToken(request.botToken, existingContent<NotificationContent.Telegram>(existingNotification)?.botToken)) {
                            "Bot token is required"
                        },
                    chatId = request.chatId.trim(),
                    messageThreadId = request.messageThreadId,
                )

            is ValidNotificationChannelNtfyRequest ->
                NotificationContent.Ntfy(
                    serverUrl = request.serverUrl.trim(),
                    topic = request.topic.trim(),
                    accessToken =
                        if (request.removeAccessToken == true) {
                            null
                        } else {
                            resolveToken(request.accessToken, existingContent<NotificationContent.Ntfy>(existingNotification)?.accessToken)
                        },
                )

            is ValidNotificationChannelGotifyRequest ->
                NotificationContent.Gotify(
                    serverUrl = request.serverUrl.trim(),
                    appToken =
                        requireNotNull(resolveToken(request.appToken, existingContent<NotificationContent.Gotify>(existingNotification)?.appToken)) {
                            "Application token is required"
                        },
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
    
    private fun resolveToken(
        incoming: String?,
        stored: String?,
    ): String? {
        incoming?.trim()?.ifBlank { null }?.let { return encryptionService.encrypt(it) }
        return stored?.let {
            if (encryptionService.isEncryptedWithCurrentKey(it)) it else encryptionService.encrypt(encryptionService.decryptIfEncrypted(it))
        }
    }

    private inline fun <reified T : NotificationContent> existingContent(existing: NotificationDto?): T? = existing?.content as? T

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
