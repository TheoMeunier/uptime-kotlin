package tmenier.fr.schedulers.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.dtos.ProbeContent
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily
import java.net.URI
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

class DockerContainerInspectorTest {
    private val inspector = DockerEngineContainerInspector(ObjectMapper())
    private val cleanups = mutableListOf<() -> Unit>()

    @AfterEach
    fun cleanup() = cleanups.forEach { runCatching { it() } }

    @Test
    fun `parses Docker hosts`() {
        assertEquals(DockerHost.Unix(Path.of("/var/run/docker.sock")), DockerHost.parse(" "))
        assertEquals(DockerHost.Unix(Path.of("/run/user/1000/docker.sock")), DockerHost.parse("unix:///run/user/1000/docker.sock"))
        assertEquals(DockerHost.Http(URI("http://docker-proxy:2375")), DockerHost.parse("tcp://docker-proxy"))
        assertEquals(DockerHost.Http(URI("http://10.0.0.5:2376")), DockerHost.parse("tcp://10.0.0.5:2376"))
        assertEquals(DockerHost.Http(URI("https://docker.example.com:2376")), DockerHost.parse("https://docker.example.com:2376"))
        assertThrows<IllegalArgumentException> { DockerHost.parse("ssh://user@host") }
        assertThrows<IllegalArgumentException> { DockerHost.parse("unix://") }
    }

    @Test
    fun `parses content-length and chunked HTTP responses`() {
        val plain = RawHttpResponse.parse("HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\n{}extra".toByteArray())
        assertEquals(200, plain.status)
        assertEquals("{}", String(plain.body))

        val chunked =
            RawHttpResponse.parse(
                "HTTP/1.1 404 Not Found\r\nTransfer-Encoding: chunked\r\n\r\n4\r\n{\"a\"\r\n3\r\n:1}\r\n0\r\n\r\n".toByteArray(),
            )
        assertEquals(404, chunked.status)
        assertEquals("{\"a\":1}", String(chunked.body))
    }

    @Test
    fun `parses the state of a healthy container`() {
        val state = inspector.parseState(ObjectMapper().readTree(HEALTHY_JSON), "fallback")

        assertEquals(DockerContainerState("web", "running", "healthy", 0, 2, null), state)
    }

    @Test
    fun `reads the container state over a Unix socket`() {
        var requestLine = ""
        val socket =
            unixServer { request ->
                requestLine = request.lineSequence().first()
                "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${HEALTHY_JSON.length}\r\n\r\n$HEALTHY_JSON"
            }

        val state = inspector.inspect(content("/web", "unix://$socket"), Duration.ofSeconds(5))

        assertEquals("GET /containers/web/json HTTP/1.1", requestLine)
        assertEquals("running", state.status)
        assertEquals("healthy", state.health)
    }

    @Test
    fun `reports a missing container`() {
        val body = """{"message":"No such container: ghost"}"""
        val socket = unixServer { "HTTP/1.1 404 Not Found\r\nContent-Length: ${body.length}\r\n\r\n$body" }

        val error = assertThrows<IllegalStateException> { inspector.inspect(content("ghost", "unix://$socket"), Duration.ofSeconds(5)) }

        assertTrue(error.message!!.contains("container \"ghost\" not found"), error.message)
    }

    @Test
    fun `times out when the Docker socket does not answer`() {
        val socket = unixServer { Thread.sleep(3_000).let { "" } }

        assertThrows<TimeoutException> { inspector.inspect(content("web", "unix://$socket"), Duration.ofSeconds(1)) }
    }

    @Test
    fun `explains when the Docker socket is missing`() {
        val error =
            assertThrows<IllegalStateException> {
                inspector.inspect(content("web", "unix:///nonexistent/docker.sock"), Duration.ofSeconds(2))
            }

        assertTrue(error.message!!.contains("cannot reach the Docker socket"), error.message)
    }

    @Test
    fun `reads the container state over TCP`() {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/containers/web/json") { exchange ->
            val bytes = """{"Name":"/web","State":{"Status":"exited","ExitCode":137,"Error":""}}""".toByteArray()
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        cleanups += { server.stop(0) }

        val state = inspector.inspect(content("web", "tcp://127.0.0.1:${server.address.port}"), Duration.ofSeconds(5))

        assertEquals("exited", state.status)
        assertEquals(137, state.exitCode)
        assertNull(state.health)
        assertNull(state.error)
    }

    private fun unixServer(respond: (String) -> String): Path {
        val directory = Files.createTempDirectory("dk")
        val socket = directory.resolve("docker.sock")
        val server = ServerSocketChannel.open(StandardProtocolFamily.UNIX)
        server.bind(UnixDomainSocketAddress.of(socket))
        cleanups += {
            server.close()
            Files.deleteIfExists(socket)
            Files.deleteIfExists(directory)
        }
        thread(isDaemon = true) {
            runCatching {
                server.accept().use { channel ->
                    val buffer = ByteBuffer.allocate(8_192)
                    val request = StringBuilder()
                    while (!request.contains("\r\n\r\n") && channel.read(buffer) != -1) {
                        buffer.flip()
                        request.append(Charsets.US_ASCII.decode(buffer))
                        buffer.clear()
                    }
                    val response = respond(request.toString())
                    channel.write(ByteBuffer.wrap(response.toByteArray()))
                }
            }
        }
        return socket
    }

    private fun content(
        name: String,
        host: String,
    ) = ProbeContent.DockerContainer(containerName = name, dockerHost = host)

    private companion object {
        const val HEALTHY_JSON =
            """{"Id":"abc","Name":"/web","RestartCount":2,""" +
                """"State":{"Status":"running","Running":true,"ExitCode":0,"Error":"","Health":{"Status":"healthy"}}}"""
    }
}
