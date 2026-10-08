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

class GotifyNotificationServiceTest {
    private lateinit var server: HttpServer
    private val http = NotificationHttpClient(Duration.ofSeconds(2), Duration.ofSeconds(2))
    private val encryption = EncryptionService("0123456789abcdef0123456789abcdef")
    private val service = GotifyNotificationService(http, NotificationDateFormatter("UTC"), encryption)
    private lateinit var baseUrl: String

    private var status = 200
    private var answer = """{"id":1,"appid":1,"message":"ok"}"""
    private var lastPath: String? = null
    private var lastQuery: String? = null
    private var lastBody: String? = null
    private var lastKey: String? = null

    @BeforeEach
    fun start() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            lastPath = exchange.requestURI.path
            lastQuery = exchange.requestURI.query
            lastKey = exchange.requestHeaders.getFirst("X-Gotify-Key")
            lastBody = exchange.requestBody.readBytes().decodeToString()
            val bytes = answer.toByteArray()
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        baseUrl = "http://127.0.0.1:${server.address.port}"
    }

    @AfterEach
    fun stop() {
        server.stop(0)
        http.close()
    }

    @Test
    fun `posts to the message endpoint with the token in a header`() {
        service.sendTest(NotificationContent.Gotify("$baseUrl/", "AppToken123"))

        assertEquals("/message", lastPath)
        assertNull(lastQuery)
        assertEquals("AppToken123", lastKey)
    }

    @Test
    fun `keeps the path of a server behind a reverse proxy`() {
        service.sendTest(NotificationContent.Gotify("$baseUrl/gotify", "AppToken123"))

        assertEquals("/gotify/message", lastPath)
    }

    @Test
    fun `decrypts a stored token`() {
        service.sendTest(NotificationContent.Gotify(baseUrl, encryption.encrypt("AppToken123")))

        assertEquals("AppToken123", lastKey)
    }

    @Test
    fun `surfaces Gotify's error without the token`() {
        status = 401
        answer =
            """{"error":"Unauthorized","errorCode":401,""" +
            """"errorDescription":"you need to provide a valid access token or user credentials to access this api"}"""

        val error = assertThrows<IllegalStateException> { service.sendTest(NotificationContent.Gotify(baseUrl, "AppToken123")) }

        assertEquals(
            "Gotify returned HTTP 401: you need to provide a valid access token or user credentials to access this api",
            error.message,
        )
        assertTrue(!error.message!!.contains("AppToken123"))
    }

    @Test
    fun `builds the payload with priority and escaped text`() {
        val result = ProbeResult(ProbeMonitorLogStatus.FAILURE, 0, "expected \"200\"", Instant.parse("2026-10-06T12:00:00Z"))

        val payload = service.buildPayload("Service api is FAILURE", result, 8)

        assertTrue(payload.contains("\"title\":\"Service api is FAILURE\""), payload)
        assertTrue(payload.contains("\"priority\":8"), payload)
        assertTrue(
            payload.contains("\"message\":\"expected \\\"200\\\"\\nStatus: FAILURE\\nDate: 06/10/2026 12:00:00 UTC\""),
            payload,
        )
    }
}
