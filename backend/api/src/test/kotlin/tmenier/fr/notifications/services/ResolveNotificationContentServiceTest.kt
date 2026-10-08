package tmenier.fr.notifications.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.NotificationDto
import tmenier.fr.databases.mappers.NotificationContentMapper
import tmenier.fr.notifications.requests.ValidNotificationChannelGotifyRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelNtfyRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelTelegramRequest
import java.util.UUID

class ResolveNotificationContentServiceTest {
    private val encryption = EncryptionService("0123456789abcdef0123456789abcdef")
    private val service = ResolveNotificationContentService(encryption, ObjectMapper())

    private fun telegram(botToken: String?) =
        ValidNotificationChannelTelegramRequest(botToken = botToken, chatId = "42").apply {
            name = "Telegram"
            notificationType = NotificationChannelsEnum.TELEGRAM
        }

    private fun ntfy(
        accessToken: String?,
        remove: Boolean = false,
    ) = ValidNotificationChannelNtfyRequest(
        serverUrl = "https://ntfy.sh",
        topic = "alerts",
        accessToken = accessToken,
        removeAccessToken = remove,
    ).apply {
        name = "ntfy"
        notificationType = NotificationChannelsEnum.NTFY
    }

    private fun gotify(appToken: String?) =
        ValidNotificationChannelGotifyRequest(serverUrl = " https://push.example.com/ ", appToken = appToken).apply {
            name = "Gotify"
            notificationType = NotificationChannelsEnum.GOTIFY
        }

    private fun stored(content: NotificationContent) =
        NotificationDto(UUID.randomUUID(), "stored", NotificationContentMapper.toEntity(content).second, false, content)

    private fun NotificationContent.botToken() = (this as NotificationContent.Telegram).botToken

    private fun NotificationContent.accessToken() = (this as NotificationContent.Ntfy).accessToken

    @Test
    fun `encrypts the bot token on creation`() {
        val token = service.resolve(telegram("123:abc"), isUpdate = false, existingNotification = null).botToken()

        assertTrue(encryption.isEncryptedWithCurrentKey(token), token)
        assertEquals("123:abc", encryption.decrypt(token))
    }

    @Test
    fun `refuses to create a Telegram channel without a token`() {
        assertThrows<IllegalArgumentException> { service.resolve(telegram(""), isUpdate = false, existingNotification = null) }
    }

    @Test
    fun `keeps the stored bot token when the field is left empty`() {
        val existing = stored(NotificationContent.Telegram(encryption.encrypt("123:abc"), "42"))

        val token = service.resolve(telegram(null), isUpdate = true, existingNotification = existing).botToken()

        assertEquals(existing.content.botToken(), token)
    }

    @Test
    fun `replaces the bot token when a new one is typed`() {
        val existing = stored(NotificationContent.Telegram(encryption.encrypt("123:abc"), "42"))

        val token = service.resolve(telegram("456:def"), isUpdate = true, existingNotification = existing).botToken()

        assertEquals("456:def", encryption.decrypt(token))
    }

    @Test
    fun `encrypts a token stored in clear before this change`() {
        val existing = stored(NotificationContent.Telegram("123:abc", "42"))

        val token = service.resolve(telegram(""), isUpdate = true, existingNotification = existing).botToken()

        assertEquals("123:abc", encryption.decrypt(token))
    }

    @Test
    fun `keeps, replaces or removes the ntfy access token`() {
        val existing = stored(NotificationContent.Ntfy("https://ntfy.sh", "alerts", encryption.encrypt("tk_old")))

        val kept = service.resolve(ntfy(""), isUpdate = true, existingNotification = existing).accessToken()
        val replaced = service.resolve(ntfy("tk_new"), isUpdate = true, existingNotification = existing).accessToken()
        val removed = service.resolve(ntfy(null, remove = true), isUpdate = true, existingNotification = existing).accessToken()

        assertEquals("tk_old", encryption.decrypt(kept!!))
        assertEquals("tk_new", encryption.decrypt(replaced!!))
        assertNull(removed)
    }

    @Test
    fun `an ntfy channel without token stays without token`() {
        assertNull(service.resolve(ntfy(null), isUpdate = false, existingNotification = null).accessToken())
    }

    @Test
    fun `tokens are masked in the API view`() {
        val telegram = NotificationContentMapper.withoutTokens(NotificationContent.Telegram("enc:v2:x", "42"))
        val ntfy = NotificationContentMapper.withoutTokens(NotificationContent.Ntfy("https://ntfy.sh", "alerts", "enc:v2:x"))

        assertEquals("", telegram.botToken())
        assertNull(ntfy.accessToken())
    }

    @Test
    fun `encrypts the Gotify token on creation and keeps it when left empty on update`() {
        val created = service.resolve(gotify("AppToken123"), isUpdate = false, existingNotification = null) as NotificationContent.Gotify

        assertEquals("https://push.example.com/", created.serverUrl)
        assertTrue(encryption.isEncryptedWithCurrentKey(created.appToken), created.appToken)
        assertEquals("AppToken123", encryption.decrypt(created.appToken))

        val kept = service.resolve(gotify(""), isUpdate = true, existingNotification = stored(created)) as NotificationContent.Gotify
        assertEquals(created.appToken, kept.appToken)
    }

    @Test
    fun `refuses to create a Gotify channel without a token`() {
        assertThrows<IllegalArgumentException> { service.resolve(gotify(null), isUpdate = false, existingNotification = null) }
    }

    @Test
    fun `never sends the Gotify token back`() {
        val gotify = NotificationContentMapper.withoutTokens(NotificationContent.Gotify("https://push.example.com", "secret"))

        assertEquals("", (gotify as NotificationContent.Gotify).appToken)
    }
}
