package tmenier.fr.notifications.services

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.NotificationDto
import tmenier.fr.databases.dtos.WebPushSubscription
import tmenier.fr.databases.mappers.NotificationContentMapper
import tmenier.fr.notifications.requests.ValidNotificationChannelGotifyRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelMailRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelMatrixRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelNtfyRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelPagerDutyRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelTelegramRequest
import tmenier.fr.notifications.requests.ValidNotificationChannelWebPushRequest
import tmenier.fr.notifications.requests.WebPushSubscriptionRequest
import java.util.UUID

class ResolveNotificationContentServiceTest {
    private val encryption = EncryptionService("0123456789abcdef0123456789abcdef")
    private val service = ResolveNotificationContentService(encryption)

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

    private fun pagerDuty(
        routingKey: String?,
        severity: String? = null,
    ) = ValidNotificationChannelPagerDutyRequest(routingKey = routingKey, severity = severity).apply {
        name = "PagerDuty"
        notificationType = NotificationChannelsEnum.PAGERDUTY
    }

    private fun gotify(appToken: String?) =
        ValidNotificationChannelGotifyRequest(serverUrl = " https://push.example.com/ ", appToken = appToken).apply {
            name = "Gotify"
            notificationType = NotificationChannelsEnum.GOTIFY
        }

    private fun matrix(accessToken: String?) =
        ValidNotificationChannelMatrixRequest(
            homeserverUrl = " https://matrix.example.org ",
            accessToken = accessToken,
            roomId = " !room:example.org ",
        ).apply {
            name = "Matrix"
            notificationType = NotificationChannelsEnum.MATRIX
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

    @Test
    fun `encrypts the Matrix token on creation and keeps it when left empty on update`() {
        val created = service.resolve(matrix("syt_secret"), isUpdate = false, existingNotification = null) as NotificationContent.Matrix

        assertEquals("https://matrix.example.org", created.homeserverUrl)
        assertEquals("!room:example.org", created.roomId)
        assertTrue(encryption.isEncryptedWithCurrentKey(created.accessToken), created.accessToken)
        assertEquals("syt_secret", encryption.decrypt(created.accessToken))

        val kept = service.resolve(matrix(""), isUpdate = true, existingNotification = stored(created)) as NotificationContent.Matrix
        assertEquals(created.accessToken, kept.accessToken)
    }

    @Test
    fun `refuses to create a Matrix channel without a token`() {
        assertThrows<IllegalArgumentException> { service.resolve(matrix(null), isUpdate = false, existingNotification = null) }
    }

    @Test
    fun `never sends the Matrix token back`() {
        val content = NotificationContentMapper.withoutTokens(NotificationContent.Matrix("https://matrix.example.org", "secret", "!room:example.org"))

        assertEquals("", (content as NotificationContent.Matrix).accessToken)
    }

    private fun webPush(vararg browsers: WebPushSubscriptionRequest) =
        ValidNotificationChannelWebPushRequest(subscriptions = browsers.toList()).apply {
            name = "Browsers"
            notificationType = NotificationChannelsEnum.WEB_PUSH
        }

    private fun newBrowser(
        endpoint: String,
        auth: String = "AuthSecret",
        label: String? = "Firefox · macOS",
    ) = WebPushSubscriptionRequest(endpoint = endpoint, p256dh = "BKey", auth = auth, label = label)

    private fun storedBrowser(endpoint: String) = WebPushSubscriptionRequest(endpoint = endpoint, p256dh = "BKey", auth = "")

    @Test
    fun `adds browsers with an encrypted secret`() {
        val created =
            service.resolve(webPush(newBrowser(" https://fcm.googleapis.com/fcm/send/a ")), isUpdate = false, existingNotification = null)
                as NotificationContent.WebPush

        val browser = created.subscriptions.single()
        assertEquals("https://fcm.googleapis.com/fcm/send/a", browser.endpoint)
        assertEquals("Firefox · macOS", browser.label)
        assertEquals("AuthSecret", encryption.decrypt(browser.auth))
    }

    @Test
    fun `keeps stored browsers, adds new ones and drops the ones left out`() {
        val created =
            service.resolve(
                webPush(newBrowser("https://push.example.com/a"), newBrowser("https://push.example.com/b")),
                isUpdate = false,
                existingNotification = null,
            ) as NotificationContent.WebPush

        val updated =
            service.resolve(
                webPush(storedBrowser("https://push.example.com/a"), newBrowser("https://web.push.apple.com/c", "Secret3", "Safari")),
                isUpdate = true,
                existingNotification = stored(created),
            ) as NotificationContent.WebPush

        assertEquals(listOf("https://push.example.com/a", "https://web.push.apple.com/c"), updated.subscriptions.map { it.endpoint })
        assertEquals(created.subscriptions[0], updated.subscriptions[0])
        assertEquals("Secret3", encryption.decrypt(updated.subscriptions[1].auth))
    }

    @Test
    fun `refuses a Web Push channel without browsers or with an unknown browser missing its keys`() {
        assertThrows<IllegalArgumentException> { service.resolve(webPush(), isUpdate = false, existingNotification = null) }
        assertThrows<IllegalArgumentException> {
            service.resolve(webPush(storedBrowser("https://push.example.com/x")), isUpdate = false, existingNotification = null)
        }
    }

    @Test
    fun `tests only the browsers whose keys are in the form`() {
        val tested =
            service.resolveForTesting(webPush(storedBrowser("https://push.example.com/a"), newBrowser("https://push.example.com/b")))
                as NotificationContent.WebPush
        assertEquals(listOf("https://push.example.com/b"), tested.subscriptions.map { it.endpoint })

        assertThrows<IllegalArgumentException> { service.resolveForTesting(webPush(storedBrowser("https://push.example.com/a"))) }
    }

    @Test
    fun `never sends the Web Push secrets back`() {
        val masked =
            NotificationContentMapper.withoutTokens(
                NotificationContent.WebPush(listOf(WebPushSubscription("https://push.example.com/x", "BKey", "secret"))),
            ) as NotificationContent.WebPush

        assertEquals("", masked.subscriptions.single().auth)
    }

    @Test
    fun `encrypts the PagerDuty key on creation and keeps it when left empty on update`() {
        val created =
            service.resolve(pagerDuty(" R0utingKey ", "Warning"), isUpdate = false, existingNotification = null) as NotificationContent.PagerDuty

        assertTrue(encryption.isEncryptedWithCurrentKey(created.routingKey), created.routingKey)
        assertEquals("R0utingKey", encryption.decrypt(created.routingKey))
        assertEquals("warning", created.severity)

        val kept = service.resolve(pagerDuty("", ""), isUpdate = true, existingNotification = stored(created)) as NotificationContent.PagerDuty
        assertEquals(created.routingKey, kept.routingKey)
        assertEquals(null, kept.severity)
    }

    @Test
    fun `refuses to create a PagerDuty channel without a key`() {
        assertThrows<IllegalArgumentException> { service.resolve(pagerDuty(null), isUpdate = false, existingNotification = null) }
    }

    @Test
    fun `never sends the PagerDuty key back`() {
        val content = NotificationContentMapper.withoutTokens(NotificationContent.PagerDuty("secret", "error"))

        assertEquals("", (content as NotificationContent.PagerDuty).routingKey)
        assertEquals("error", content.severity)
    }

    private fun mail(
        password: String?,
        username: String = "user",
    ) = ValidNotificationChannelMailRequest(
        hostname = "smtp.example.com",
        port = 587,
        username = username,
        password = password,
        from = "from@example.com",
        to = "to@example.com",
    ).apply {
        name = "Mail"
        notificationType = NotificationChannelsEnum.MAIL
    }

    private fun NotificationContent.password() = (this as NotificationContent.Mail).password

    @Test
    fun `keeps the stored mail password when the field is left empty on update`() {
        val existing =
            stored(
                NotificationContent.Mail(
                    hostname = "smtp.example.com",
                    port = 587,
                    username = "user",
                    password = encryption.encrypt("secret"),
                    from = "from@example.com",
                    to = "to@example.com",
                ),
            )

        val password = service.resolve(mail(null), isUpdate = true, existingNotification = existing).password()

        assertEquals(existing.content.password(), password)
    }

    @Test
    fun `replaces the mail password when a new one is typed`() {
        val password = service.resolve(mail("new-secret"), isUpdate = true, existingNotification = null).password()

        assertEquals("new-secret", encryption.decrypt(requireNotNull(password)))
    }

    @Test
    fun `a test of an edited mail channel reuses the stored password when the field is empty`() {
        val existing =
            stored(
                NotificationContent.Mail(
                    hostname = "smtp.example.com",
                    port = 587,
                    username = "user",
                    password = encryption.encrypt("secret"),
                    from = "from@example.com",
                    to = "to@example.com",
                ),
            )

        assertEquals(existing.content.password(), service.resolveForTesting(mail(""), existing).password())
        assertEquals("typed", service.resolveForTesting(mail("typed"), existing).password())
        assertThrows<IllegalArgumentException> { service.resolveForTesting(mail(null)) }
    }

    @Test
    fun `accepts a mail username that is not an email address`() {
        val validator = Validation.buildDefaultValidatorFactory().validator

        for (username in listOf("resend", "AKIAIOSFODNN7EXAMPLE")) {
            val request = mail("secret", username)
            val invalidFields = validator.validate(request).map { it.propertyPath.toString() }

            assertFalse("username" in invalidFields, "username '$username' should be valid")
            val content = service.resolve(request, isUpdate = false, existingNotification = null) as NotificationContent.Mail
            assertEquals(username, content.username)
        }
    }

    @Test
    fun `rejects a blank mail username`() {
        val validator = Validation.buildDefaultValidatorFactory().validator
        val invalidFields = validator.validate(mail("secret", "  ")).map { it.propertyPath.toString() }

        assertTrue("username" in invalidFields)
    }
}
