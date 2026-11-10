package tmenier.fr.notifications.templates

import io.vertx.core.Vertx
import io.vertx.ext.mail.StartTLSOptions
import org.jboss.logging.Logger
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.common.enums.monitors.SmtpSecurity
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.notifications.NotificationDateFormatter
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmailNotificationServiceTest {
    private val vertx = Vertx.vertx()
    private val service =
        EmailNotificationService(
            vertx,
            Logger.getLogger(EmailNotificationServiceTest::class.java),
            EncryptionService("0123456789abcdef0123456789abcdef"),
            NotificationDateFormatter("UTC"),
            "1.6.0",
        )

    private val at = Instant.parse("2026-11-10T13:55:00Z")

    @AfterAll
    fun close() {
        vertx.close()
    }

    @Test
    fun `failure e-mail carries the error, target and date in both parts`() {
        val email = service.failureEmail(probe(), result(ProbeMonitorLogStatus.FAILURE, "Connection refused", 503))

        assertEquals("🔴 api is down", email.subject)
        listOf(email.html, email.text).forEach { part ->
            assertTrue(part.contains("Connection refused"), part)
            assertTrue(part.contains("https://api.example.com/health"), part)
            assertTrue(part.contains("10/11/2026 13:55:00 UTC"), part)
            assertTrue(part.contains("503"), part)
            assertTrue(part.contains("Uptime Kotlin 1.6.0"), part)
        }
        assertTrue(email.html.contains("#DC2626"))
    }

    @Test
    fun `a warning is announced as degraded`() {
        val email = service.failureEmail(probe(), result(ProbeMonitorLogStatus.WARNING, "Slow response"))

        assertEquals("🟡 api is degraded", email.subject)
        assertTrue(email.html.contains("#D97706"))
    }

    @Test
    fun `recovery e-mail shows the downtime`() {
        val email = service.successEmail(probe(), result(ProbeMonitorLogStatus.SUCCESS, "OK", 200), Duration.ofMinutes(12))

        assertEquals("✅ api is back up (down for 12m)", email.subject)
        assertTrue(email.text.contains("Downtime:"), email.text)
        assertTrue(email.html.contains("12m"))
        assertFalse(email.html.contains(">Error<"), "no error block on recovery")
    }

    @Test
    fun `reminder e-mail shows its index`() {
        val email = service.reminderEmail(probe(), result(ProbeMonitorLogStatus.FAILURE, "Timeout"), 3)

        assertEquals("🟠 api is still down (reminder #3)", email.subject)
        assertTrue(email.html.contains("reminder #3"))
    }

    @Test
    fun `probe values are html-escaped`() {
        val email =
            service.failureEmail(
                probe(name = "<script>alert(1)</script>"),
                result(ProbeMonitorLogStatus.FAILURE, "expected \"<ok>\" & got 'ko'"),
            )

        assertFalse(email.html.contains("<script>"))
        assertTrue(email.html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"))
        assertTrue(email.html.contains("expected &quot;&lt;ok&gt;&quot; &amp; got &#39;ko&#39;"))
        assertTrue(email.text.contains("expected \"<ok>\" & got 'ko'"), "plain text is not escaped")
    }

    @Test
    fun `long error messages are truncated`() {
        val email = service.failureEmail(probe(), result(ProbeMonitorLogStatus.FAILURE, "x".repeat(10_000)))

        assertFalse(email.text.contains("x".repeat(EmailTemplate.MAX_DETAILS_LENGTH + 1)))
        assertTrue(email.text.contains("…"))
    }

    @Test
    fun `test e-mail shows the smtp server without scheme`() {
        val content =
            NotificationContent.Mail(
                hostname = "https://smtp.example.com",
                port = 587,
                starttls = true,
                username = "bot",
                from = "uptime@example.com",
                to = "ops@example.com",
            )

        val email = service.testEmail(content, at)

        assertTrue(email.text.contains("smtp.example.com:587"), email.text)
        assertFalse(email.text.contains("https://smtp"), email.text)
    }

    private fun mail(
        security: SmtpSecurity? = null,
        starttls: Boolean = false,
    ) = NotificationContent.Mail(
        hostname = "smtp.example.com",
        port = 465,
        starttls = starttls,
        security = security,
        username = "bot",
        from = "uptime@example.com",
        to = "ops@example.com",
    )

    @Test
    fun `smtps uses implicit tls`() {
        val config = service.mailConfig(mail(SmtpSecurity.SMTPS), "secret")

        assertTrue(config.isSsl)
        assertEquals(StartTLSOptions.DISABLED, config.starttls)
    }

    @Test
    fun `starttls upgrades a plain connection`() {
        val config = service.mailConfig(mail(SmtpSecurity.STARTTLS), "secret")

        assertFalse(config.isSsl)
        assertEquals(StartTLSOptions.REQUIRED, config.starttls)
    }

    @Test
    fun `ignore tls sends in clear`() {
        val config = service.mailConfig(mail(SmtpSecurity.IGNORE_TLS), "secret")

        assertFalse(config.isSsl)
        assertEquals(StartTLSOptions.DISABLED, config.starttls)
    }

    @Test
    fun `legacy channels without security fall back to the starttls flag`() {
        assertEquals(SmtpSecurity.STARTTLS, mail(starttls = true).resolvedSecurity())
        assertEquals(SmtpSecurity.IGNORE_TLS, mail(starttls = false).resolvedSecurity())
        assertEquals(SmtpSecurity.SMTPS, mail(SmtpSecurity.SMTPS, starttls = true).resolvedSecurity())
    }

    @Test
    fun `writes html previews for manual review`() {
        val dir = File("build/email-previews").apply { mkdirs() }
        val failure = result(ProbeMonitorLogStatus.FAILURE, "HTTP 503 Service Unavailable\nupstream connect error", 503)
        mapOf(
            "failure" to service.failureEmail(probe(), failure),
            "degraded" to service.failureEmail(probe(), result(ProbeMonitorLogStatus.WARNING, "Response time 4812 ms > 2000 ms")),
            "reminder" to service.reminderEmail(probe(), failure, 2),
            "recovery" to service.successEmail(probe(), result(ProbeMonitorLogStatus.SUCCESS, "OK", 200), Duration.ofMinutes(47)),
            "test" to
                service.testEmail(
                    NotificationContent.Mail("smtp.example.com", 587, true, "bot", null, "uptime@example.com", "ops@example.com"),
                    at,
                ),
        ).forEach { (name, email) ->
            File(dir, "$name.html").writeText(email.html)
            File(dir, "$name.txt").writeText("Subject: ${email.subject}\n\n${email.text}")
        }
        assertTrue(File(dir, "failure.html").exists())
    }

    private fun result(
        status: ProbeMonitorLogStatus,
        message: String,
        statusCode: Int? = null,
    ) = ProbeResult(status = status, responseTime = 184, message = message, runAt = at, statusCode = statusCode)

    private fun probe(name: String = "api") =
        ProbeDTO(
            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            name = name,
            interval = 60,
            timeout = 10,
            retry = 0,
            intervalRetry = 60,
            enabled = true,
            protocol = ProbeProtocol.HTTP,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.FAILURE,
            content =
                ProbeContent.Http(
                    url = "https://api.example.com/health",
                    notificationCertified = false,
                    ignoreCertificateErrors = false,
                    httpCodeAllowed = emptyList(),
                ),
            url = "https://api.example.com/health",
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )
}
