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
    ): NotificationContent =
        when (notification.type) {
            NotificationChannelsEnum.DISCORD -> {
                objectMapper.treeToValue(notification.content, NotificationContent.Discord::class.java)
            }

            NotificationChannelsEnum.TEAMS -> {
                objectMapper.treeToValue(notification.content, NotificationContent.Teams::class.java)
            }

            NotificationChannelsEnum.SLACK -> {
                objectMapper.treeToValue(notification.content, NotificationContent.Slack::class.java)
            }

            NotificationChannelsEnum.MAIL -> {
                val node = notification.content as ObjectNode
                if (isPassword) node.remove("password")
                objectMapper.treeToValue(node, NotificationContent.Mail::class.java)
            }

            NotificationChannelsEnum.WEBHOOK -> {
                objectMapper.treeToValue(notification.content, NotificationContent.Webhook::class.java)
            }
        } as NotificationContent

    fun toEntity(content: NotificationContent): Pair<JsonNode, NotificationChannelsEnum> {
        val type =
            when (content) {
                is NotificationContent.Discord -> NotificationChannelsEnum.DISCORD
                is NotificationContent.Teams -> NotificationChannelsEnum.TEAMS
                is NotificationContent.Slack -> NotificationChannelsEnum.SLACK
                is NotificationContent.Mail -> NotificationChannelsEnum.MAIL
                is NotificationContent.Webhook -> NotificationChannelsEnum.WEBHOOK
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
