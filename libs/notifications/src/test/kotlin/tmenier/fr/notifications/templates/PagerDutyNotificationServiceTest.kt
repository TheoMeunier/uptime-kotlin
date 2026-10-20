package tmenier.fr.notifications.templates

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
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

class PagerDutyNotificationServiceTest {
    private lateinit var server: HttpServer
    private lateinit var service: PagerDutyNotificationService
    private val http = NotificationHttpClient(Duration.ofSeconds(2), Duration.ofSeconds(2))
    private val encryption = EncryptionService("0123456789abcdef0123456789abcdef")
    private val json = ObjectMapper()

    private var status = 202
    private var answer = """{"status":"success","message":"Event processed","dedup_key":"k"}"""
    private val bodies = mutableListOf<JsonNode>()
    private var lastPath: String? = null

    private val probeId = UUID.fromString("11111111-2222-3333-4444-555555555555")
    private val failure =
        ProbeResult(ProbeMonitorLogStatus.FAILURE, 120, "expected \"200\", got 503", Instant.parse("2026-10-20T01:00:00Z"), statusCode = 503)

    @BeforeEach
    fun start() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            lastPath = exchange.requestURI.path
            bodies.add(json.readTree(exchange.requestBody.readBytes()))
            val bytes = answer.toByteArray()
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        service = PagerDutyNotificationService(http, encryption, "http://127.0.0.1:${server.address.port}/v2/enqueue")
    }

    @AfterEach
    fun stop() {
        server.stop(0)
        http.close()
    }

    @Test
    fun `a failure triggers an alert keyed on the probe`() {
        service.sendFailure(NotificationContent.PagerDuty("RoutingKey123"), probe(), failure)

        val body = bodies.single()
        assertEquals("/v2/enqueue", lastPath)
        assertEquals("RoutingKey123", body["routing_key"].asText())
        assertEquals("trigger", body["event_action"].asText())
        assertEquals("uptime-kotlin:probe:$probeId", body["dedup_key"].asText())
        assertEquals("Service api is FAILURE: expected \"200\", got 503", body["payload"]["summary"].asText())
        assertEquals("https://api.example.com/health", body["payload"]["source"].asText())
        assertEquals("critical", body["payload"]["severity"].asText())
        assertEquals("2026-10-20T01:00:00Z", body["payload"]["timestamp"].asText())
        assertEquals("api", body["payload"]["component"].asText())
        assertEquals(503, body["payload"]["custom_details"]["status_code"].asInt())
        assertEquals(probeId.toString(), body["payload"]["custom_details"]["probe_id"].asText())
    }

    @Test
    fun `a reminder reuses the same key so PagerDuty folds it into the open alert`() {
        service.sendFailure(NotificationContent.PagerDuty("k"), probe(), failure)
        service.sendReminder(NotificationContent.PagerDuty("k"), probe(), failure, 2)

        assertEquals(bodies[0]["dedup_key"], bodies[1]["dedup_key"])
        assertEquals("trigger", bodies[1]["event_action"].asText())
        assertTrue(bodies[1]["payload"]["summary"].asText().startsWith("Service api is still FAILURE (reminder #2)"))
    }

    @Test
    fun `a recovery resolves the alert of the probe`() {
        val success = failure.copy(status = ProbeMonitorLogStatus.SUCCESS, message = "OK")
        service.sendSuccess(NotificationContent.PagerDuty("k"), probe(), success, Duration.ofMinutes(5))

        val body = bodies.single()
        assertEquals("resolve", body["event_action"].asText())
        assertEquals("uptime-kotlin:probe:$probeId", body["dedup_key"].asText())
        assertNull(body["payload"])
    }

    @Test
    fun `uses the configured severity and ignores an unknown one`() {
        service.sendFailure(NotificationContent.PagerDuty("k", "warning"), probe(), failure)
        service.sendFailure(NotificationContent.PagerDuty("k", "bogus"), probe(), failure)

        assertEquals("warning", bodies[0]["payload"]["severity"].asText())
        assertEquals("critical", bodies[1]["payload"]["severity"].asText())
    }

    @Test
    fun `falls back to the probe name when it has no URL`() {
        service.sendFailure(NotificationContent.PagerDuty("k"), probe(url = null), failure)

        assertEquals("api", bodies.single()["payload"]["source"].asText())
    }

    @Test
    fun `the test opens an info alert and resolves it at once`() {
        service.sendTest(NotificationContent.PagerDuty("k", "critical"))

        assertEquals(2, bodies.size)
        assertEquals("trigger", bodies[0]["event_action"].asText())
        assertEquals("info", bodies[0]["payload"]["severity"].asText())
        assertEquals("resolve", bodies[1]["event_action"].asText())
        assertEquals(bodies[0]["dedup_key"], bodies[1]["dedup_key"])
        assertTrue(bodies[0]["dedup_key"].asText().startsWith("uptime-kotlin:test:"))
    }

    @Test
    fun `two tests never share an alert`() {
        service.sendTest(NotificationContent.PagerDuty("k"))
        service.sendTest(NotificationContent.PagerDuty("k"))

        assertNotEquals(bodies[0]["dedup_key"], bodies[2]["dedup_key"])
    }

    @Test
    fun `decrypts a stored key`() {
        service.sendFailure(NotificationContent.PagerDuty(encryption.encrypt("RoutingKey123")), probe(), failure)

        assertEquals("RoutingKey123", bodies.single()["routing_key"].asText())
    }

    @Test
    fun `surfaces PagerDuty's error without the key`() {
        status = 400
        answer = """{"status":"invalid event","message":"Event object is invalid","errors":["'routing_key' RoutingKey123 is invalid"]}"""

        val error =
            assertThrows<IllegalStateException> {
                service.sendFailure(NotificationContent.PagerDuty("RoutingKey123"), probe(), failure)
            }

        assertEquals("PagerDuty returned HTTP 400: Event object is invalid ('routing_key' *** is invalid)", error.message)
        assertFalse(error.message!!.contains("RoutingKey123"))
    }

    @Test
    fun `a failed trigger during the test does not send the resolve`() {
        status = 429
        answer = "Too many requests"

        assertThrows<IllegalStateException> { service.sendTest(NotificationContent.PagerDuty("k")) }
        assertEquals(1, bodies.size)
    }

    @Test
    fun `keeps the summary within PagerDuty's limit`() {
        val long = failure.copy(message = "x".repeat(5000))
        service.sendFailure(NotificationContent.PagerDuty("k"), probe(), long)

        assertEquals(1024, bodies.single()["payload"]["summary"].asText().length)
    }

    private fun probe(url: String? = "https://api.example.com/health") =
        ProbeDTO(
            id = probeId,
            name = "api",
            interval = 60,
            timeout = 10,
            retry = 0,
            intervalRetry = 60,
            enabled = true,
            protocol = ProbeProtocol.HTTP,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.FAILURE,
            content = CONTENT,
            url = url,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )

    private companion object {
        val CONTENT: ProbeContent =
            ProbeContent.Http(
                url = "https://api.example.com/health",
                notificationCertified = false,
                ignoreCertificateErrors = false,
                httpCodeAllowed = emptyList(),
            )
    }
}
