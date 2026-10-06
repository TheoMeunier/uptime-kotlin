package tmenier.fr.notifications.templates

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.notifications.NotificationDateFormatter
import tmenier.fr.notifications.NotificationHttpClient
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant

class TelegramNotificationServiceTest {
    private lateinit var server: HttpServer
    private val http = NotificationHttpClient(Duration.ofSeconds(2), Duration.ofSeconds(2))
    private lateinit var service: TelegramNotificationService

    private var status = 200
    private var answer = """{"ok":true,"result":{}}"""
    private var lastPath: String? = null
    private var lastBody: String? = null

    private val content = NotificationContent.Telegram("123456:ABC-def_ghi", "-1001234567890")

    @BeforeEach
    fun start() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            lastPath = exchange.requestURI.path
            lastBody = exchange.requestBody.readBytes().decodeToString()
            val bytes = answer.toByteArray()
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        service =
            TelegramNotificationService(http, NotificationDateFormatter("UTC"), "http://127.0.0.1:${server.address.port}/")
    }

    @AfterEach
    fun stop() {
        server.stop(0)
        http.close()
    }

    @Test
    fun `posts to the bot sendMessage endpoint in HTML mode`() {
        service.sendTest(content)

        assertEquals("/bot123456:ABC-def_ghi/sendMessage", lastPath)
        val body = lastBody!!
        assertTrue(body.contains("\"chat_id\":\"-1001234567890\""), body)
        assertTrue(body.contains("\"parse_mode\":\"HTML\""), body)
        assertFalse(body.contains("message_thread_id"), body)
    }

    @Test
    fun `sends the topic id when one is configured`() {
        service.sendTest(content.copy(messageThreadId = 42))

        assertTrue(lastBody!!.contains("\"message_thread_id\":42"), lastBody)
    }

    @Test
    fun `surfaces Telegram's description without leaking the token`() {
        status = 400
        answer = """{"ok":false,"error_code":400,"description":"Bad Request: chat not found"}"""

        val error = assertThrows<IllegalStateException> { service.sendTest(content) }

        assertEquals("Telegram returned HTTP 400: Bad Request: chat not found", error.message)
        assertFalse(error.message!!.contains(content.botToken))
    }

    @Test
    fun `escapes HTML from the probe name and message`() {
        val text =
            service.buildMessage(
                "🔴",
                "Service <api> & co is FAILURE",
                "expected <200>",
                ProbeMonitorLogStatus.FAILURE,
                Instant.parse("2026-10-06T12:00:00Z"),
            )

        assertTrue(text.startsWith("🔴 <b>Service &lt;api&gt; &amp; co is FAILURE</b>"), text)
        assertTrue(text.contains("expected &lt;200&gt;"), text)
        assertTrue(text.contains("<b>Date:</b> 06/10/2026 12:00:00 UTC"), text)
    }

    @Test
    fun `escapes quotes and line breaks in the JSON payload`() {
        val payload = service.buildPayload(content, "a \"quoted\"\nline\\")

        assertTrue(payload.contains("\"text\":\"a \\\"quoted\\\"\\nline\\\\\""), payload)
    }
}
