package tmenier.fr.notifications.templates

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
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
import java.util.UUID

class MatrixNotificationServiceTest {
    private lateinit var server: HttpServer
    private val http = NotificationHttpClient(Duration.ofSeconds(2), Duration.ofSeconds(2))
    private val encryption = EncryptionService("0123456789abcdef0123456789abcdef")
    private val service = MatrixNotificationService(http, NotificationDateFormatter("UTC"), encryption)
    private lateinit var baseUrl: String

    private var status = 200
    private var answer = """{"event_id":"${'$'}abc:example.org"}"""
    private var lastMethod: String? = null
    private var lastRawPath: String? = null
    private var lastBody: String? = null
    private var lastAuthorization: String? = null

    @BeforeEach
    fun start() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            lastMethod = exchange.requestMethod
            lastRawPath = exchange.requestURI.rawPath
            lastAuthorization = exchange.requestHeaders.getFirst("Authorization")
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
    fun `puts the message in the room with the token as a bearer`() {
        service.sendTest(NotificationContent.Matrix("$baseUrl/", "syt_secret", "!room:example.org"))

        assertEquals("PUT", lastMethod)
        assertTrue(
            lastRawPath!!.startsWith("/_matrix/client/v3/rooms/%21room%3Aexample.org/send/m.room.message/uptime-kotlin.test."),
            lastRawPath,
        )
        assertEquals("Bearer syt_secret", lastAuthorization)
        assertTrue(lastBody!!.contains("\"msgtype\":\"m.text\""), lastBody)
        assertTrue(lastBody!!.contains("\"format\":\"org.matrix.custom.html\""), lastBody)
    }

    @Test
    fun `keeps the path of a homeserver behind a reverse proxy`() {
        service.sendTest(NotificationContent.Matrix("$baseUrl/matrix", "syt_secret", "!room:example.org"))

        assertTrue(lastRawPath!!.startsWith("/matrix/_matrix/client/v3/rooms/"), lastRawPath)
    }

    @Test
    fun `decrypts a stored token`() {
        service.sendTest(NotificationContent.Matrix(baseUrl, encryption.encrypt("syt_secret"), "!room:example.org"))

        assertEquals("Bearer syt_secret", lastAuthorization)
    }

    @Test
    fun `surfaces the Matrix error without the token`() {
        status = 403
        answer = """{"errcode":"M_FORBIDDEN","error":"User @bot:example.org not in room !room:example.org"}"""

        val error =
            assertThrows<IllegalStateException> {
                service.sendTest(NotificationContent.Matrix(baseUrl, "syt_secret", "!room:example.org"))
            }

        assertEquals("Matrix returned HTTP 403: M_FORBIDDEN: User @bot:example.org not in room !room:example.org", error.message)
        assertTrue(!error.message!!.contains("syt_secret"))
    }

    @Test
    fun `gives the same transaction id to a retried delivery and a new one to the next event`() {
        val probeId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val runAt = Instant.parse("2026-10-26T08:00:00Z")

        assertEquals(service.txnId("failure", probeId, runAt), service.txnId("failure", probeId, runAt))
        assertNotEquals(service.txnId("failure", probeId, runAt), service.txnId("recovery", probeId, runAt))
        assertNotEquals(service.txnId("reminder", probeId, runAt, 1), service.txnId("reminder", probeId, runAt, 2))
        assertNotEquals(service.txnId("failure", probeId, runAt), service.txnId("failure", probeId, runAt.plusSeconds(60)))
    }

    @Test
    fun `builds a plain and an html body with escaped text`() {
        val result = ProbeResult(ProbeMonitorLogStatus.FAILURE, 0, "expected <200>\ngot \"500\"", Instant.parse("2026-10-06T12:00:00Z"))

        val payload = service.buildPayload("🔴", "Service api & co is FAILURE", result.message, result.status, result.runAt)

        assertTrue(
            payload.contains(
                "\"body\":\"🔴 Service api & co is FAILURE\\n\\nexpected <200>\\ngot \\\"500\\\"\\n\\nStatus: FAILURE\\nDate: 06/10/2026 12:00:00 UTC\"",
            ),
            payload,
        )
        assertTrue(
            payload.contains(
                "\"formatted_body\":\"🔴 <strong>Service api &amp; co is FAILURE</strong><br><br>expected &lt;200&gt;<br>got \\\"500\\\"" +
                    "<br><br><strong>Status:</strong> FAILURE<br><strong>Date:</strong> 06/10/2026 12:00:00 UTC\"",
            ),
            payload,
        )
    }
}
