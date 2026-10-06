package tmenier.fr.notifications.requests

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NotificationRequestValidationTest {
    private val objectMapper =
        ObjectMapper()
            .findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun violations(
        json: String,
        group: Class<*>,
    ): Set<String> =
        validator
            .validate(objectMapper.readValue(json, BaseStoreNotificationRequest::class.java), group)
            .map { it.propertyPath.toString() }
            .toSet()

    private val discord =
        """{"notification_type":"DISCORD","name":"Discord","webhook_url":"https://discord.com/api/webhooks/1/abc","username":"Uptime"}"""
    private val webhook = """{"notification_type":"WEBHOOK","name":"Hook","url":"https://example.com/hook","method":"POST"}"""

    private fun mail(password: String?) =
        """
        {"notification_type":"MAIL","name":"Mail","hostname":"https://smtp.example.com","port":587,
         "username":"me@example.com","password":${password?.let { "\"$it\"" } ?: "null"},
         "from":"me@example.com","to":"you@example.com"}
        """.trimIndent()

    @Test
    fun `valid payloads from the frontend are accepted on create and update`() {
        for (json in listOf(discord, webhook, mail("secret"))) {
            assertEquals(emptySet<String>(), violations(json, OnCreate::class.java), json)
            assertEquals(emptySet<String>(), violations(json, OnUpdate::class.java), json)
        }
    }

    @Test
    fun `an update keeps the stored mail password when none is sent`() {
        assertEquals(emptySet<String>(), violations(mail(null), OnUpdate::class.java))
        assertEquals(setOf("password"), violations(mail(null), OnCreate::class.java))
    }

    @Test
    fun `create and update enforce the ungrouped constraints`() {
        val invalid = """{"notification_type":"WEBHOOK","name":"","url":"not a url","method":"POST"}"""

        for (group in listOf(OnCreate::class.java, OnUpdate::class.java)) {
            val found = violations(invalid, group)
            assertTrue(found.containsAll(setOf("name", "url")), "$group: $found")
        }
    }
}
