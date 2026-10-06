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

    fun extractPassword(content: JsonNode): String? = content.get("password")?.asText()

    fun toDTO(
        notification: NotificationsChannelEntity,
        isPassword: Boolean = true,
    ): NotificationContent {
        val node = notification.content
        if (notification.type == NotificationChannelsEnum.MAIL && isPassword) (node as ObjectNode).remove("password")
        return objectMapper.treeToValue(node, contentClass(notification.type))
    }

    private fun contentClass(type: NotificationChannelsEnum): Class<out NotificationContent> =
        when (type) {
            NotificationChannelsEnum.DISCORD -> NotificationContent.Discord::class.java
            NotificationChannelsEnum.TEAMS -> NotificationContent.Teams::class.java
            NotificationChannelsEnum.SLACK -> NotificationContent.Slack::class.java
            NotificationChannelsEnum.MAIL -> NotificationContent.Mail::class.java
            NotificationChannelsEnum.WEBHOOK -> NotificationContent.Webhook::class.java
            NotificationChannelsEnum.TELEGRAM -> NotificationContent.Telegram::class.java
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
            }
        val jsonNode = objectMapper.valueToTree<JsonNode>(content)

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

    fun toDto(entity: NotificationsChannelEntity): NotificationDto =
        NotificationDto(
            id = entity.id,
            name = entity.name,
            type = entity.type,
            isDefault = entity.isDefault,
            content = NotificationContentMapper.toDTO(entity),
        )

    fun toSmallDto(entity: NotificationsChannelEntity): ListingNotificationsDto =
        ListingNotificationsDto(
            id = entity.id,
            name = entity.name,
            isDefault = entity.isDefault,
        )

    fun toShowDto(entity: NotificationsChannelEntity): ShowNotificationsDto =
        ShowNotificationsDto(
            id = entity.id,
            name = entity.name,
            notificationType = entity.type,
            content = NotificationContentMapper.toDTO(entity, false),
            isDefault = entity.isDefault,
            createdAt = entity.createdAt,
        )
}
