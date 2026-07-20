package tmenier.fr.schedulers.templates

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.HttpCodeEnum
import tmenier.fr.common.enums.monitors.HttpMethodEnum
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.SslCertificateService
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class ProbeProtocolHttpTest {
    private var server: HttpServer? = null

    @AfterEach
    fun stopServer() {
        server?.stop(0)
    }

    @Test
    fun `executes an authenticated scenario and validates response content`() {
        server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/first") { exchange ->
                    assertEquals("POST", exchange.requestMethod)
                    assertEquals("Bearer secret", exchange.requestHeaders.getFirst("Authorization"))
                    assertEquals("probe", exchange.requestHeaders.getFirst("X-Test"))
                    assertEquals("payload", exchange.requestBody.bufferedReader().readText())
                    val response = "{\"status\":\"ok\"}".toByteArray()
                    exchange.responseHeaders.add("X-Result", "ready")
                    exchange.sendResponseHeaders(200, response.size.toLong())
                    exchange.responseBody.use { it.write(response) }
                }
                start()
            }

        val content =
            defaultContent().copy(
                headers = mapOf("X-Test" to "probe"),
                authentication =
                    ProbeContent.HttpAuthentication(
                        type = ProbeContent.HttpAuthenticationType.BEARER,
                        token = "secret",
                    ),
                steps =
                    listOf(
                        ProbeContent.HttpStep(
                            name = "API",
                            url = url("/first"),
                            method = HttpMethodEnum.POST,
                            body = "payload",
                            assertions =
                                listOf(
                                    ProbeContent.HttpAssertion(
                                        ProbeContent.HttpAssertionType.JSON_EQUALS,
                                        expected = "ok",
                                        path = "status",
                                    ),
                                    ProbeContent.HttpAssertion(
                                        ProbeContent.HttpAssertionType.RESPONSE_HEADER_EQUALS,
                                        expected = "ready",
                                        header = "X-Result",
                                    ),
                                ),
                        ),
                    ),
            )

        val result = executor().execute(probe(content), content, true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertEquals(200, result.statusCode)
        assertTrue(result.message.contains("1/1 API"))
    }

    @Test
    fun `sends basic credentials when the Probe requires authentication`() {
        var authorization: String? = null
        server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/basic") { exchange ->
                    authorization = exchange.requestHeaders.getFirst("Authorization")
                    exchange.sendResponseHeaders(204, -1)
                    exchange.close()
                }
                start()
            }
        val content =
            defaultContent().copy(
                url = url("/basic"),
                httpCodeAllowed = listOf(HttpCodeEnum.NO_CONTENT),
                authentication =
                    ProbeContent.HttpAuthentication(
                        type = ProbeContent.HttpAuthenticationType.BASIC,
                        username = "monitor",
                        password = "secret",
                    ),
            )

        val result = executor().execute(probe(content), content, true)

        val encoded = Base64.getEncoder().encodeToString("monitor:secret".toByteArray(StandardCharsets.UTF_8))
        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertEquals("Basic $encoded", authorization)
    }

    @Test
    fun `executes a Probe without authentication and validates text content`() {
        var authorization: String? = "not-called"
        server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/public") { exchange ->
                    authorization = exchange.requestHeaders.getFirst("Authorization")
                    val response = "service is healthy".toByteArray()
                    exchange.sendResponseHeaders(200, response.size.toLong())
                    exchange.responseBody.use { it.write(response) }
                }
                start()
            }
        val content =
            defaultContent().copy(
                url = url("/public"),
                authentication = null,
                assertions =
                    listOf(
                        ProbeContent.HttpAssertion(
                            type = ProbeContent.HttpAssertionType.TEXT_CONTAINS,
                            expected = "healthy",
                        ),
                    ),
            )

        val result = executor().execute(probe(content), content, true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertEquals(null, authorization)
    }

    @Test
    fun `fails when redirects are disabled and redirect status is not allowed`() {
        server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/redirect") { exchange ->
                    exchange.responseHeaders.add("Location", url("/target"))
                    exchange.sendResponseHeaders(302, -1)
                    exchange.close()
                }
                createContext("/target") { exchange ->
                    exchange.sendResponseHeaders(200, -1)
                    exchange.close()
                }
                start()
            }
        val content = defaultContent().copy(url = url("/redirect"), followRedirects = false)

        val result = executor().execute(probe(content), content, true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("unexpected HTTP status 302"))
    }

    @Test
    fun `follows redirects when enabled`() {
        val targetCalls = AtomicInteger()
        server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/redirect") { exchange ->
                    exchange.responseHeaders.add("Location", url("/target"))
                    exchange.sendResponseHeaders(302, -1)
                    exchange.close()
                }
                createContext("/target") { exchange ->
                    targetCalls.incrementAndGet()
                    exchange.sendResponseHeaders(200, -1)
                    exchange.close()
                }
                start()
            }
        val content = defaultContent().copy(url = url("/redirect"), followRedirects = true)

        val result = executor().execute(probe(content), content, true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertEquals(1, targetCalls.get())
    }

    @Test
    fun `fails when response latency exceeds the configured threshold`() {
        server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/slow") { exchange ->
                    Thread.sleep(75)
                    exchange.sendResponseHeaders(200, -1)
                    exchange.close()
                }
                start()
            }
        val content = defaultContent().copy(url = url("/slow"), maxLatencyMs = 10)

        val result = executor().execute(probe(content), content, true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("exceeds 10ms"))
    }

    @Test
    fun `stops a scenario after the first failing step`() {
        val secondStepCalls = AtomicInteger()
        server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/failure") { exchange ->
                    exchange.sendResponseHeaders(500, -1)
                    exchange.close()
                }
                createContext("/second") { exchange ->
                    secondStepCalls.incrementAndGet()
                    exchange.sendResponseHeaders(200, -1)
                    exchange.close()
                }
                start()
            }
        val content =
            defaultContent().copy(
                steps =
                    listOf(
                        ProbeContent.HttpStep(name = "Failure", url = url("/failure")),
                        ProbeContent.HttpStep(name = "Second", url = url("/second")),
                    ),
            )

        val result = executor().execute(probe(content), content, true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertEquals(0, secondStepCalls.get())
        assertTrue(result.message.contains("Failure: unexpected HTTP status 500"))
    }

    @Test
    fun `executes every step of a successful scenario in order`() {
        val calls = mutableListOf<String>()
        server =
            HttpServer.create(InetSocketAddress(0), 0).apply {
                createContext("/first-step") { exchange ->
                    calls += "first"
                    exchange.sendResponseHeaders(200, -1)
                    exchange.close()
                }
                createContext("/second-step") { exchange ->
                    calls += "second"
                    exchange.sendResponseHeaders(200, -1)
                    exchange.close()
                }
                start()
            }
        val content =
            defaultContent().copy(
                steps =
                    listOf(
                        ProbeContent.HttpStep(name = "First", url = url("/first-step")),
                        ProbeContent.HttpStep(name = "Second", url = url("/second-step")),
                    ),
            )

        val result = executor().execute(probe(content), content, true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertEquals(listOf("first", "second"), calls)
        assertTrue(result.message.contains("1/2 First"))
        assertTrue(result.message.contains("2/2 Second"))
    }

    private fun executor() = ProbeProtocolHttp(SslCertificateService(), jacksonObjectMapper())

    private fun defaultContent() =
        ProbeContent.Http(
            url = url("/"),
            notificationCertified = false,
            ignoreCertificateErrors = false,
            httpCodeAllowed = listOf(HttpCodeEnum.OK),
        )

    private fun probe(content: ProbeContent.Http) =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "HTTP test",
            interval = 60,
            timeout = 5,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.HTTP,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

    private fun url(path: String) = "http://localhost:${server?.address?.port ?: 0}$path"
}
