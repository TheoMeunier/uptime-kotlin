package tmenier.fr.monitors.actions.notifications

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.monitors.dtos.requests.BaseStoreNotificationRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelDiscordRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelMailRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelSlackRequest
import tmenier.fr.monitors.dtos.requests.ValidNotificationChannelTeamsRequest
import tmenier.fr.monitors.notifications.NotificationService
import tmenier.fr.monitors.notifications.dto.NotificationContent
import tmenier.fr.monitors.notifications.dto.NotificationTestingDto

@ApplicationScoped
class TestingNotificationAction(
    private val notificationService: NotificationService
) {

    fun execute(payload: BaseStoreNotificationRequest) {
        val notification = NotificationTestingDto(
            type = payload.notificationType,
            content = mapPayloadToNotificationContent(payload)
        )

        notificationService.sendTest(notification)
    }

    private fun mapPayloadToNotificationContent(payload: BaseStoreNotificationRequest): NotificationContent {
        return when (payload) {
            is ValidNotificationChannelDiscordRequest -> NotificationContent.Discord(
                webhookUrl = payload.urlWebhook,
                username = payload.nameReboot
            )

            is ValidNotificationChannelTeamsRequest -> NotificationContent.Teams(
                webhookUrl = payload.urlWebhook,
                username = payload.nameReboot
            )

            is ValidNotificationChannelSlackRequest -> NotificationContent.Slack(
                webhookUrl = payload.urlWebhook,
                username = payload.nameReboot
            )

            is ValidNotificationChannelMailRequest -> NotificationContent.Mail(
                hostname = payload.hostname,
                port = payload.port,
                starttls = payload.starttls ?: false,
                username = payload.username,
                password = payload.password,
                from = payload.mailFrom,
                to = payload.mailTo,
            )

            else -> throw IllegalArgumentException("Unknown notification type: ${payload.notificationType}")
        }
    }

}
