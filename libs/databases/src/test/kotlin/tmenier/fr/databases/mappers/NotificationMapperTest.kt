package tmenier.fr.databases.mappers

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.entities.NotificationsChannelEntity
import java.time.Instant
import java.util.UUID

class NotificationMapperTest {
    private fun mailEntity() =
        NotificationsChannelEntity().apply {
            id = UUID.randomUUID()
            name = "SMTP"
            type = NotificationChannelsEnum.MAIL
            content =
                ObjectMapper().readTree(
                    """
                    {"hostname":"smtp.example.com","port":587,"username":"user","password":"v2:encrypted",
                     "from":"from@example.com","to":"to@example.com"}
                    """,
                )
        }

    @Test
    fun `default dto hides the mail password`() {
        val content = NotificationMapper.toDto(mailEntity()).content as NotificationContent.Mail

        assertNull(content.password)
    }

    @Test
    fun `dto with secrets keeps the stored mail password`() {
        val content = NotificationMapper.toDto(mailEntity(), withSecrets = true).content as NotificationContent.Mail

        assertEquals("v2:encrypted", content.password)
    }

    @Test
    fun `hiding the password leaves the entity untouched`() {
        val entity = mailEntity()

        NotificationMapper.toDto(entity)

        assertEquals("v2:encrypted", entity.content?.get("password")?.asText())
        val content = NotificationMapper.toDto(entity, withSecrets = true).content as NotificationContent.Mail
        assertEquals("v2:encrypted", content.password)
    }

    @Test
    fun `stored mail content keeps the password`() {
        val (node, _) =
            NotificationContentMapper.toEntity(
                NotificationContent.Mail(
                    hostname = "smtp.example.com",
                    port = 587,
                    username = "user",
                    password = "v2:encrypted",
                    from = "from@example.com",
                    to = "to@example.com",
                ),
            )

        assertEquals("v2:encrypted", node.get("password")?.asText())
    }

    private fun channel(
        type: NotificationChannelsEnum,
        json: String,
    ) = NotificationsChannelEntity().apply {
        id = UUID.randomUUID()
        name = type.name
        this.type = type
        content = ObjectMapper().readTree(json)
        createdAt = Instant.now()
    }

    private val webhookChannels =
        listOf(
            channel(NotificationChannelsEnum.DISCORD, """{"webhookUrl":"https://discord.com/api/webhooks/1/secret","username":null}"""),
            channel(NotificationChannelsEnum.SLACK, """{"webhookUrl":"https://hooks.slack.com/services/T/B/secret","username":null}"""),
            channel(NotificationChannelsEnum.TEAMS, """{"webhookUrl":"https://example.webhook.office.com/secret","username":null}"""),
            channel(NotificationChannelsEnum.WEBHOOK, """{"method":"POST","url":"https://example.com/hook?token=secret"}"""),
        )

    @Test
    fun `show dto hides webhook urls by default`() {
        webhookChannels.forEach { entity ->
            val content = NotificationMapper.toShowDto(entity).content

            val url =
                when (content) {
                    is NotificationContent.Discord -> content.webhookUrl
                    is NotificationContent.Slack -> content.webhookUrl
                    is NotificationContent.Teams -> content.webhookUrl
                    is NotificationContent.Webhook -> content.url
                    else -> error("Unexpected content $content")
                }
            assertEquals("", url, "${entity.type} leaks its webhook URL")
        }
    }

    @Test
    fun `show dto reveals webhook urls when asked`() {
        val discord = NotificationMapper.toShowDto(webhookChannels[0], revealWebhookUrls = true).content as NotificationContent.Discord
        val webhook = NotificationMapper.toShowDto(webhookChannels[3], revealWebhookUrls = true).content as NotificationContent.Webhook

        assertEquals("https://discord.com/api/webhooks/1/secret", discord.webhookUrl)
        assertEquals("https://example.com/hook?token=secret", webhook.url)
    }
}
