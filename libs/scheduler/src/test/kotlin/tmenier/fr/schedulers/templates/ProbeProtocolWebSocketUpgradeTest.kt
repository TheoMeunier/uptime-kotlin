package tmenier.fr.schedulers.templates

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.ProbeHttpClientFactory
import java.net.ServerSocket
import java.net.Socket
import java.net.http.HttpClient
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.UUID
import kotlin.concurrent.thread

class ProbeProtocolWebSocketUpgradeTest {
    private val executor =
        ProbeProtocolWebSocketUpgrade(
            ProbeHttpClientFactory { _, _, timeout -> HttpClient.newBuilder().connectTimeout(timeout).build() },
        )
    private val servers = mutableListOf<ServerSocket>()

    @AfterEach
    fun closeServers() = servers.forEach { it.close() }

    @Test
    fun `returns success when the server switches protocols`() {
        val port = server { socket, request -> socket.reply(handshake(request)) }

        val result = executor.execute(probe(), content("ws://127.0.0.1:$port/socket"), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status, result.message)
        assertEquals(101, result.statusCode)
        assertTrue(result.message.startsWith("WebSocket upgrade successful"))
    }

    @Test
    fun `returns success when the requested subprotocol is accepted`() {
        val port = server { socket, request -> socket.reply(handshake(request, subprotocol = "graphql-ws")) }

        val result = executor.execute(probe(), content("ws://127.0.0.1:$port", subprotocol = "graphql-ws"), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status, result.message)
    }

    @Test
    fun `fails when the server ignores the requested subprotocol`() {
        val port = server { socket, request -> socket.reply(handshake(request)) }

        val result = executor.execute(probe(), content("ws://127.0.0.1:$port", subprotocol = "graphql-ws"), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("graphql-ws"), result.message)
    }

    @Test
    fun `reports the HTTP status when the server refuses the upgrade`() {
        val port = server { socket, _ -> socket.reply("HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n") }

        val result = executor.execute(probe(), content("ws://127.0.0.1:$port"), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertEquals(200, result.statusCode)
        assertTrue(result.message.contains("HTTP 200 instead of 101"), result.message)
    }

    @Test
    fun `returns warning on timeout when a retry remains`() {
        val port = server { _, _ -> Thread.sleep(3_000) }

        val result = executor.execute(probe(timeout = 1), content("ws://127.0.0.1:$port"), false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
        assertTrue(result.message.contains("timed out after 1s"), result.message)
    }

    @Test
    fun `fails when nothing listens on the port`() {
        val port = ServerSocket(0).use { it.localPort }

        val result = executor.execute(probe(), content("ws://127.0.0.1:$port"), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.startsWith("WebSocket upgrade failed"))
    }

    @Test
    fun `rejects non WebSocket URLs`() {
        assertThrows<IllegalArgumentException> { executor.parseUrl("https://example.com") }
        assertThrows<IllegalArgumentException> { executor.parseUrl("ws:///path") }
        assertEquals("wss", executor.parseUrl(" wss://example.com/socket ").scheme)
    }

    private fun server(handler: (Socket, Map<String, String>) -> Unit): Int {
        val server = ServerSocket(0)
        servers += server
        thread(isDaemon = true) {
            runCatching {
                server.accept().use { socket ->
                    handler(socket, readHeaders(socket))
                    // Keep the connection open until the client closes it.
                    runCatching { socket.getInputStream().readAllBytes() }
                }
            }
        }
        return server.localPort
    }

    private fun readHeaders(socket: Socket): Map<String, String> {
        val reader = socket.getInputStream().bufferedReader(Charsets.ISO_8859_1)
        return generateSequence { reader.readLine() }
            .takeWhile { it.isNotEmpty() }
            .drop(1)
            .associate { it.substringBefore(':').trim().lowercase() to it.substringAfter(':').trim() }
    }

    private fun handshake(
        request: Map<String, String>,
        subprotocol: String? = null,
    ): String {
        val key = request.getValue("sec-websocket-key")
        val accept =
            Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-1").digest((key + WEBSOCKET_GUID).toByteArray()),
            )
        val protocolHeader = subprotocol?.let { "Sec-WebSocket-Protocol: $it\r\n" }.orEmpty()
        return "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\n" +
            "Sec-WebSocket-Accept: $accept\r\n$protocolHeader\r\n"
    }

    private fun Socket.reply(response: String) {
        getOutputStream().apply {
            write(response.toByteArray(Charsets.ISO_8859_1))
            flush()
        }
    }

    private fun content(
        url: String,
        subprotocol: String? = null,
    ) = ProbeContent.WebSocketUpgrade(url = url, subprotocol = subprotocol)

    private fun probe(timeout: Int = 5) =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "WebSocket test",
            interval = 60,
            timeout = timeout,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.WEBSOCKET,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content("ws://localhost"),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

    private companion object {
        const val WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
    }
}
