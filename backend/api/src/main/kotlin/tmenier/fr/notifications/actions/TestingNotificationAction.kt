package tmenier.fr.notifications.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.monitors.notifications.NotificationService
import tmenier.fr.monitors.notifications.dto.NotificationContent
import tmenier.fr.monitors.notifications.dto.NotificationTestingDto
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelDiscordRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelMailRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelSlackRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelTeamsRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelWebhookRequest

@ApplicationScoped
class TestingNotificationAction(
    private val notificationService: NotificationService,
) {
    fun execute(payload: BaseStoreNotificationRequest) {
        val notification =
            NotificationTestingDto(
                type = payload.notificationType,
                content = mapPayloadToNotificationContent(payload),
            )

        notificationService.sendTest(notification)
    }

    private fun mapPayloadToNotificationContent(payload: BaseStoreNotificationRequest): NotificationContent =
        when (payload) {
            is ValidNotificationChannelDiscordRequest -> {
                NotificationContent.Discord(
                    webhookUrl = payload.webhookUrl,
                    username = payload.username,
                )
            }

            is ValidNotificationChannelTeamsRequest -> {
                NotificationContent.Teams(
                    webhookUrl = payload.webhookUrl,
                    username = payload.username,
                )
            }

            is ValidNotificationChannelSlackRequest -> {
                NotificationContent.Slack(
                    webhookUrl = payload.webhookUrl,
                    username = payload.username,
                )
            }

            is ValidNotificationChannelWebhookRequest -> {
                NotificationContent.Webhook(
                    url = payload.url,
                    method = payload.method,
                )
            }

            is ValidNotificationChannelMailRequest -> {
                NotificationContent.Mail(
                    hostname = payload.hostname,
                    port = payload.port,
                    starttls = payload.starttls ?: false,
                    username = payload.username,
                    password = payload.password,
                    from = payload.from,
                    to = payload.to,
                )
            }

            else -> {
                throw IllegalArgumentException("Unknown notification type: ${payload.notificationType}")
            }
        }
}
