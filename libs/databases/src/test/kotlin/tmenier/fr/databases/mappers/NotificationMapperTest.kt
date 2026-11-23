package tmenier.fr.databases.mappers

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.entities.NotificationsChannelEntity
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
}
