package tmenier.fr.notifications.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.NotificationDto
import tmenier.fr.notifications.requests.BaseStoreNotificationRequest
import tmenier.fr.notifications.requests.OnCreate
import tmenier.fr.notifications.requests.OnUpdate
import tmenier.fr.notifications.requests.ValidNotificationChannelGotifyRequest
import java.util.UUID

class GotifyNotificationRequestTest {
    private val encryptionService = EncryptionService("0123456789abcdef0123456789abcdef")

    // Same naming strategy as quarkus.jackson.property-naming-strategy.
    private val objectMapper =
        ObjectMapper()
            .findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    private val service = ResolveNotificationContentService(encryptionService, objectMapper)
    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun parse(json: String): BaseStoreNotificationRequest = objectMapper.readValue(json, BaseStoreNotificationRequest::class.java)

    private fun gotifyJson(
        token: String? = "app-token",
        priority: Int? = 8,
        serverUrl: String = "https://gotify.example.com",
    ) = """
        {
            "notification_type": "GOTIFY",
            "name": "Gotify",
            "is_default": false,
            "server_url": "$serverUrl",
            "token": ${token?.let { "\"$it\"" } ?: "null"},
            "priority": ${priority ?: "null"}
        }
        """.trimIndent()

    private fun existing(token: String) =
        NotificationDto(
            id = UUID.randomUUID(),
            name = "Gotify",
            type = NotificationChannelsEnum.GOTIFY,
            isDefault = false,
            content = NotificationContent.Gotify("https://gotify.example.com", token, 5),
        )

    @Test
    fun `the payload sent by the frontend deserializes into a Gotify request`() {
        val request = parse(gotifyJson()) as ValidNotificationChannelGotifyRequest

        assertEquals(NotificationChannelsEnum.GOTIFY, request.notificationType)
        assertEquals("https://gotify.example.com", request.serverUrl)
        assertEquals("app-token", request.token)
        assertEquals(8, request.priority)
    }

    @Test
    fun `creation stores the token encrypted`() {
        val content = service.resolve(parse(gotifyJson()), isUpdate = false, existingNotification = null) as NotificationContent.Gotify

        assertNotEquals("app-token", content.token)
        assertTrue(encryptionService.isEncryptedWithCurrentKey(content.token!!))
        assertEquals("app-token", encryptionService.decrypt(content.token!!))
        assertEquals(8, content.priority)
    }

    @Test
    fun `a missing priority falls back to the Gotify default`() {
        val content = service.resolve(parse(gotifyJson(priority = null)), false, null) as NotificationContent.Gotify

        assertEquals(NotificationContent.Gotify.DEFAULT_GOTIFY_PRIORITY, content.priority)
    }

    @Test
    fun `an update with a blank token keeps the stored one`() {
        val stored = encryptionService.encrypt("stored-token")

        for (token in listOf(null, "")) {
            val content = service.resolve(parse(gotifyJson(token = token)), true, existing(stored)) as NotificationContent.Gotify
            assertEquals(stored, content.token)
        }
    }

    @Test
    fun `an update with a new token replaces the stored one`() {
        val content =
            service.resolve(
                parse(gotifyJson(token = "new-token")),
                true,
                existing(encryptionService.encrypt("stored-token")),
            ) as NotificationContent.Gotify

        assertEquals("new-token", encryptionService.decrypt(content.token!!))
    }

    @Test
    fun `a test without a token is a bad request, not a server error`() {
        assertThrows<BadRequestException> { service.resolveForTesting(parse(gotifyJson(token = null))) }
    }

    @Test
    fun `a test encrypts the token so that the sender can decrypt it`() {
        val content = service.resolveForTesting(parse(gotifyJson())) as NotificationContent.Gotify

        assertEquals("app-token", encryptionService.decrypt(content.token!!))
    }

    @Test
    fun `creation requires a token`() {
        val violations = validator.validate(parse(gotifyJson(token = "")), OnCreate::class.java)

        assertTrue(violations.any { it.propertyPath.toString() == "token" }, violations.toString())
    }

    @Test
    fun `create and update reject an out of range priority and an invalid server URL`() {
        for (group in listOf(OnCreate::class.java, OnUpdate::class.java)) {
            val violations =
                validator
                    .validate(parse(gotifyJson(priority = 11, serverUrl = "not a url")), group)
                    .map { it.propertyPath.toString() }
                    .toSet()

            assertEquals(setOf("priority", "serverUrl"), violations, "group ${group.simpleName}")
        }
    }

    @Test
    fun `an update does not require the token again`() {
        val violations = validator.validate(parse(gotifyJson(token = null)), OnUpdate::class.java)

        assertTrue(violations.isEmpty(), violations.toString())
    }
}
