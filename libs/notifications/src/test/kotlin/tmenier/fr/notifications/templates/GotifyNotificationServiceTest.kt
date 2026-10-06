package tmenier.fr.notifications.templates

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.notifications.NotificationHttpClient
import java.net.InetSocketAddress
import java.time.Duration
import java.time.Instant
import java.util.UUID

class GotifyNotificationServiceTest {
    private data class Received(
        val method: String,
        val path: String,
        val token: String?,
        val contentType: String?,
        val body: String,
    )

    private lateinit var server: HttpServer
    private val received = mutableListOf<Received>()
    private var responseStatus = 200
    private val http = NotificationHttpClient(Duration.ofSeconds(2), Duration.ofSeconds(2))
    private val encryption = EncryptionService("test-only-master-key-do-not-use-in-production")
    private val service = GotifyNotificationService(http, encryption)
    private lateinit var serverUrl: String

    @BeforeEach
    fun startFakeGotify() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            received +=
                Received(
                    method = exchange.requestMethod,
                    path = exchange.requestURI.path,
                    token = exchange.requestHeaders.getFirst("X-Gotify-Key"),
                    contentType = exchange.requestHeaders.getFirst("Content-Type"),
                    body = exchange.requestBody.readAllBytes().decodeToString(),
                )
            val reply = """{"id":1}""".toByteArray()
            exchange.sendResponseHeaders(responseStatus, reply.size.toLong())
            exchange.responseBody.use { it.write(reply) }
        }
        server.start()
        serverUrl = "http://127.0.0.1:${server.address.port}"
    }

    @AfterEach
    fun stop() {
        server.stop(0)
        http.close()
    }

    private fun content(
        url: String = serverUrl,
        priority: Int = 8,
    ) = NotificationContent.Gotify(serverUrl = url, token = encryption.encrypt("app-token"), priority = priority)

    private val probe =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "API \"prod\"",
            interval = 60,
            timeout = 5,
            retry = 0,
            intervalRetry = 60,
            enabled = true,
            protocol = ProbeProtocol.HTTP,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.FAILURE,
            content = ProbeContent.Tcp(url = "localhost", tcpPort = 80),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )

    private fun result(status: ProbeMonitorLogStatus) =
        ProbeResult(
            status = status,
            responseTime = 12,
            message = "HTTP check failed:\nconnection refused",
            runAt = Instant.parse("2026-10-06T12:00:00Z"),
        )

    @Test
    fun `failure posts to the message endpoint with the decrypted application token`() {
        service.sendFailure(content(), probe, result(ProbeMonitorLogStatus.FAILURE))

        val request = received.single()
        assertEquals("POST", request.method)
        assertEquals("/message", request.path)
        assertEquals("app-token", request.token)
        assertEquals("application/json", request.contentType)
        assertTrue(request.body.contains("\"priority\": 8"), request.body)
        assertTrue(request.body.contains("""Service API \"prod\" - FAILURE"""), request.body)
        assertTrue(request.body.contains("""HTTP check failed:\nconnection refused"""), request.body)
    }

    @Test
    fun `recovery and reminder describe the event in the title`() {
        service.sendSuccess(content(), probe, result(ProbeMonitorLogStatus.SUCCESS), Duration.ofMinutes(3))
        service.sendReminder(content(), probe, result(ProbeMonitorLogStatus.FAILURE), 2)

        assertTrue(received[0].body.contains("SUCCESS (down for"), received[0].body)
        assertTrue(received[1].body.contains("still FAILURE (reminder #2)"), received[1].body)
    }

    @Test
    fun `a trailing slash or a sub-path on the server URL is respected`() {
        service.sendTest(content(url = "$serverUrl/gotify/"))

        assertEquals("/gotify/message", received.single().path)
    }

    @Test
    fun `a rejected token fails the delivery so that it is retried`() {
        responseStatus = 401

        val error = assertThrows<IllegalStateException> { service.sendTest(content()) }
        assertTrue(error.message!!.contains("HTTP 401"))
    }

    @Test
    fun `a channel without a token fails instead of sending an unauthenticated request`() {
        assertThrows<IllegalArgumentException> {
            service.sendTest(NotificationContent.Gotify(serverUrl = serverUrl, token = null))
        }
        assertTrue(received.isEmpty())
    }
}
