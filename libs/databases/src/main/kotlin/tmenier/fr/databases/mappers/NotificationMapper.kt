package tmenier.fr.databases.mappers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.ListingNotificationsDto
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.NotificationDto
import tmenier.fr.databases.dtos.ShowNotificationsDto
import tmenier.fr.databases.entities.NotificationsChannelEntity

object NotificationContentMapper {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun toDTO(
        notification: NotificationsChannelEntity,
        isPassword: Boolean = true,
    ): NotificationContent {
        val node = notification.content?.deepCopy<JsonNode>()
        if (notification.type == NotificationChannelsEnum.MAIL && isPassword) (node as ObjectNode).remove("password")
        val content = objectMapper.treeToValue(node, contentClass(notification.type))

        // The password is write-only for Jackson: take it straight from the stored JSON rather than relying on the conversion.
        if (!isPassword && content is NotificationContent.Mail) {
            return content.copy(password = node?.get("password")?.takeUnless { it.isNull }?.asText())
        }
        return content
    }

    fun withoutTokens(content: NotificationContent): NotificationContent =
        when (content) {
            is NotificationContent.Telegram -> content.copy(botToken = "")
            is NotificationContent.Ntfy -> content.copy(accessToken = null)
            is NotificationContent.Gotify -> content.copy(appToken = "")
            is NotificationContent.PagerDuty -> content.copy(routingKey = "")
            is NotificationContent.Matrix -> content.copy(accessToken = "")
            is NotificationContent.WebPush -> content.copy(subscriptions = content.subscriptions.map { it.copy(auth = "") })
            is NotificationContent.Mail -> content.copy(security = content.resolvedSecurity())
            else -> content
        }

    fun withoutWebhookUrls(content: NotificationContent): NotificationContent =
        when (content) {
            is NotificationContent.Discord -> content.copy(webhookUrl = "")
            is NotificationContent.Slack -> content.copy(webhookUrl = "")
            is NotificationContent.Teams -> content.copy(webhookUrl = "")
            is NotificationContent.Webhook -> content.copy(url = "")
            else -> content
        }

    private fun contentClass(type: NotificationChannelsEnum): Class<out NotificationContent> =
        when (type) {
            NotificationChannelsEnum.DISCORD -> NotificationContent.Discord::class.java
            NotificationChannelsEnum.TEAMS -> NotificationContent.Teams::class.java
            NotificationChannelsEnum.SLACK -> NotificationContent.Slack::class.java
            NotificationChannelsEnum.MAIL -> NotificationContent.Mail::class.java
            NotificationChannelsEnum.WEBHOOK -> NotificationContent.Webhook::class.java
            NotificationChannelsEnum.TELEGRAM -> NotificationContent.Telegram::class.java
            NotificationChannelsEnum.NTFY -> NotificationContent.Ntfy::class.java
            NotificationChannelsEnum.GOTIFY -> NotificationContent.Gotify::class.java
            NotificationChannelsEnum.WEB_PUSH -> NotificationContent.WebPush::class.java
            NotificationChannelsEnum.PAGERDUTY -> NotificationContent.PagerDuty::class.java
            NotificationChannelsEnum.MATRIX -> NotificationContent.Matrix::class.java
        }

    fun toEntity(content: NotificationContent): Pair<JsonNode, NotificationChannelsEnum> {
        val type =
            when (content) {
                is NotificationContent.Discord -> NotificationChannelsEnum.DISCORD
                is NotificationContent.Teams -> NotificationChannelsEnum.TEAMS
                is NotificationContent.Slack -> NotificationChannelsEnum.SLACK
                is NotificationContent.Mail -> NotificationChannelsEnum.MAIL
                is NotificationContent.Webhook -> NotificationChannelsEnum.WEBHOOK
                is NotificationContent.Telegram -> NotificationChannelsEnum.TELEGRAM
                is NotificationContent.Ntfy -> NotificationChannelsEnum.NTFY
                is NotificationContent.Gotify -> NotificationChannelsEnum.GOTIFY
                is NotificationContent.WebPush -> NotificationChannelsEnum.WEB_PUSH
                is NotificationContent.PagerDuty -> NotificationChannelsEnum.PAGERDUTY
                is NotificationContent.Matrix -> NotificationChannelsEnum.MATRIX
            }
        val jsonNode = objectMapper.valueToTree<JsonNode>(content)
        if (content is NotificationContent.Mail && content.password != null) {
            (jsonNode as ObjectNode).put("password", content.password)
        }

        return jsonNode to type
    }
}

object NotificationMapper {
    fun toEntity(dto: NotificationDto): NotificationsChannelEntity =
        NotificationsChannelEntity().apply {
            id = dto.id
            name = dto.name
            type = dto.type
            isDefault = dto.isDefault
            content = NotificationContentMapper.toEntity(dto.content).first
        }

    fun toDto(
        entity: NotificationsChannelEntity,
        withSecrets: Boolean = false,
    ): NotificationDto =
        NotificationDto(
            id = entity.id,
            name = entity.name,
            type = entity.type,
            isDefault = entity.isDefault,
            content = NotificationContentMapper.toDTO(entity, isPassword = !withSecrets),
        )

    fun toSmallDto(entity: NotificationsChannelEntity): ListingNotificationsDto =
        ListingNotificationsDto(
            id = entity.id,
            name = entity.name,
            isDefault = entity.isDefault,
        )

    fun toShowDto(
        entity: NotificationsChannelEntity,
        revealWebhookUrls: Boolean = false,
    ): ShowNotificationsDto =
        ShowNotificationsDto(
            id = entity.id,
            name = entity.name,
            notificationType = entity.type,
            content =
                NotificationContentMapper
                    .withoutTokens(NotificationContentMapper.toDTO(entity, false))
                    .let { if (revealWebhookUrls) it else NotificationContentMapper.withoutWebhookUrls(it) },
            isDefault = entity.isDefault,
            createdAt = entity.createdAt,
        )
}
