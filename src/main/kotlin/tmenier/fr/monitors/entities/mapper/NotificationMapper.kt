package tmenier.fr.monitors.entities.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import tmenier.fr.monitors.dtos.responses.ListingNotificationsDto
import tmenier.fr.monitors.entities.NotificationsChannelEntity
import tmenier.fr.monitors.enums.NotificationChannelsEnum
import tmenier.fr.monitors.notifications.dto.NotificationContent

object NotificationContentMapper {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun toDTO(notification: NotificationsChannelEntity): NotificationContent =
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
                objectMapper.treeToValue(notification.content, NotificationContent.Mail::class.java)
            }

            else -> {
                // TODO: create default notification content
            }
        } as NotificationContent

    fun toEntity(content: NotificationContent): Pair<JsonNode, NotificationChannelsEnum> {
        val type =
            when (content) {
                is NotificationContent.Discord -> NotificationChannelsEnum.DISCORD
                is NotificationContent.Teams -> NotificationChannelsEnum.TEAMS
                is NotificationContent.Slack -> NotificationChannelsEnum.SLACK
                is NotificationContent.Mail -> NotificationChannelsEnum.MAIL
            }
        val jsonNode = objectMapper.valueToTree<JsonNode>(content)

        return jsonNode to type
    }
}

fun NotificationsChannelEntity.toListingsDTO() =
    ListingNotificationsDto(
        id = id,
        name = name,
        isDefault = isDefault,
    )
