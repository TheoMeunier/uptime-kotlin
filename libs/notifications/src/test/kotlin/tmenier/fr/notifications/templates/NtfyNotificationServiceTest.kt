package tmenier.fr.notifications.templates

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.notifications.NotificationDateFormatter
import tmenier.fr.notifications.NotificationHttpClient
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant

class NtfyNotificationServiceTest {
    private lateinit var server: HttpServer
    private val http = NotificationHttpClient(Duration.ofSeconds(2), Duration.ofSeconds(2))
    private val encryption = EncryptionService("0123456789abcdef0123456789abcdef")
    private val service = NtfyNotificationService(http, NotificationDateFormatter("UTC"), encryption)
    private lateinit var content: NotificationContent.Ntfy

    private var status = 200
    private var answer = """{"id":"abc","event":"message"}"""
    private var lastPath: String? = null
    private var lastBody: String? = null
    private var lastAuthorization: String? = null

    @BeforeEach
    fun start() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            lastPath = exchange.requestURI.path
            lastAuthorization = exchange.requestHeaders.getFirst("Authorization")
            lastBody = exchange.requestBody.readBytes().decodeToString()
            val bytes = answer.toByteArray()
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        content = NotificationContent.Ntfy("http://127.0.0.1:${server.address.port}/", "uptime-alerts")
    }

    @AfterEach
    fun stop() {
        server.stop(0)
        http.close()
    }

    @Test
    fun `publishes JSON to the server root without credentials by default`() {
        service.sendTest(content)

        assertEquals("/", lastPath)
        assertNull(lastAuthorization)
        assertTrue(lastBody!!.contains("\"topic\":\"uptime-alerts\""), lastBody)
    }

    @Test
    fun `sends the access token as a bearer token`() {
        service.sendTest(content.copy(accessToken = "tk_secret"))

        assertEquals("Bearer tk_secret", lastAuthorization)
    }

    @Test
    fun `decrypts a stored access token`() {
        service.sendTest(content.copy(accessToken = encryption.encrypt("tk_secret")))

        assertEquals("Bearer tk_secret", lastAuthorization)
    }

    @Test
    fun `surfaces ntfy's error without the token`() {
        status = 403
        answer = """{"code":40301,"http":403,"error":"forbidden","link":"https://ntfy.sh/docs/publish/"}"""

        val error = assertThrows<IllegalStateException> { service.sendTest(content.copy(accessToken = "tk_secret")) }

        assertEquals("ntfy returned HTTP 403: forbidden", error.message)
    }

    @Test
    fun `builds the payload with priority, tag and escaped text`() {
        val result = ProbeResult(ProbeMonitorLogStatus.FAILURE, 0, "expected \"200\"", Instant.parse("2026-10-06T12:00:00Z"))

        val payload = service.buildPayload("alerts", "Service api is FAILURE", result, 4, "rotating_light")

        assertTrue(payload.contains("\"priority\":4"), payload)
        assertTrue(payload.contains("\"tags\":[\"rotating_light\"]"), payload)
        assertTrue(
            payload.contains("\"message\":\"expected \\\"200\\\"\\nStatus: FAILURE\\nDate: 06/10/2026 12:00:00 UTC\""),
            payload,
        )
    }
}
