package tmenier.fr.schedulers.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import java.io.ByteArrayOutputStream
import java.net.StandardProtocolFamily
import java.net.URI
import java.net.URLEncoder
import java.net.UnixDomainSocketAddress
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/** State of a container as reported by GET /containers/{id}/json. */
data class DockerContainerState(
    val name: String,
    val status: String,
    val health: String? = null,
    val exitCode: Int? = null,
    val restartCount: Int = 0,
    val error: String? = null,
)

fun interface DockerContainerInspector {
    fun inspect(
        content: ProbeContent.DockerContainer,
        timeout: Duration,
    ): DockerContainerState
}

@ApplicationScoped
class DockerEngineContainerInspector(
    private val objectMapper: ObjectMapper,
) : DockerContainerInspector {
    override fun inspect(
        content: ProbeContent.DockerContainer,
        timeout: Duration,
    ): DockerContainerState {
        val containerName = content.containerName.trim().removePrefix("/")
        require(containerName.isNotEmpty()) { "Container name is required" }
        val path = "/containers/${URLEncoder.encode(containerName, StandardCharsets.UTF_8)}/json"

        val response =
            when (val host = DockerHost.parse(content.dockerHost)) {
                is DockerHost.Unix -> unixGet(host.socket, path, timeout)
                is DockerHost.Http -> httpGet(host.baseUri, path, timeout)
            }

        return when (response.status) {
            200 -> parseState(objectMapper.readTree(response.body), containerName)
            404 -> throw IllegalStateException("container \"$containerName\" not found on ${content.dockerHost.trim()}")
            else -> throw IllegalStateException("Docker Engine answered HTTP ${response.status}: ${errorMessage(response.body)}")
        }
    }

    internal fun parseState(
        json: JsonNode,
        fallbackName: String,
    ): DockerContainerState {
        val state = json.path("State")
        require(state.isObject) { "unexpected Docker Engine response (no State)" }
        return DockerContainerState(
            name =
                json
                    .path("Name")
                    .asText("")
                    .removePrefix("/")
                    .ifBlank { fallbackName },
            status = state.path("Status").asText("unknown"),
            health =
                state
                    .path("Health")
                    .path("Status")
                    .takeIf { it.isTextual }
                    ?.asText(),
            exitCode = state.path("ExitCode").takeIf { it.isNumber }?.asInt(),
            restartCount = json.path("RestartCount").asInt(0),
            error = state.path("Error").asText("").ifBlank { null },
        )
    }

    private fun errorMessage(body: ByteArray): String =
        runCatching { objectMapper.readTree(body).path("message").asText("") }
            .getOrNull()
            ?.ifBlank { null }
            ?: String(body, StandardCharsets.UTF_8).trim().take(MAX_ERROR_LENGTH)

    private fun httpGet(
        baseUri: URI,
        path: String,
        timeout: Duration,
    ): RawHttpResponse =
        HttpClient
            .newBuilder()
            .connectTimeout(timeout)
            .build()
            .use { client ->
                val request =
                    HttpRequest
                        .newBuilder(baseUri.resolve(path))
                        .timeout(timeout)
                        .header("Accept", "application/json")
                        .GET()
                        .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
                RawHttpResponse(response.statusCode(), response.body())
            }

    internal fun unixGet(
        socket: Path,
        path: String,
        timeout: Duration,
    ): RawHttpResponse {
        val channel = SocketChannel.open(StandardProtocolFamily.UNIX)
        val exchange =
            CompletableFuture.supplyAsync({
                channel.connect(UnixDomainSocketAddress.of(socket))
                val request =
                    "GET $path HTTP/1.1\r\nHost: docker\r\nAccept: application/json\r\n" +
                        "User-Agent: uptime-kotlin\r\nConnection: close\r\n\r\n"
                val buffer = ByteBuffer.wrap(request.toByteArray(StandardCharsets.US_ASCII))
                while (buffer.hasRemaining()) channel.write(buffer)
                readAll(channel)
            }, IO_EXECUTOR)

        return try {
            RawHttpResponse.parse(exchange.get(timeout.toMillis(), TimeUnit.MILLISECONDS))
        } catch (error: TimeoutException) {
            throw TimeoutException("Docker Engine did not answer within ${timeout.toSeconds()}s")
        } catch (error: ExecutionException) {
            val cause = error.cause ?: error
            if (cause is java.io.IOException) {
                throw IllegalStateException("cannot reach the Docker socket $socket: ${cause.message ?: cause.javaClass.simpleName}", cause)
            }
            throw cause
        } finally {
            channel.close()
        }
    }

    private fun readAll(channel: SocketChannel): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteBuffer.allocate(BUFFER_SIZE)
        while (channel.read(buffer) != -1) {
            buffer.flip()
            output.write(buffer.array(), 0, buffer.limit())
            buffer.clear()
            check(output.size() <= MAX_RESPONSE_BYTES) { "Docker Engine response too large" }
        }
        return output.toByteArray()
    }

    private companion object {
        const val BUFFER_SIZE = 8_192
        const val MAX_RESPONSE_BYTES = 4 * 1024 * 1024
        const val MAX_ERROR_LENGTH = 300
        val IO_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor()
    }
}

sealed interface DockerHost {
    data class Unix(
        val socket: Path,
    ) : DockerHost

    data class Http(
        val baseUri: URI,
    ) : DockerHost

    companion object {
        private const val DEFAULT_TCP_PORT = 2375

        fun parse(value: String): DockerHost {
            val trimmed = value.trim().ifBlank { ProbeContent.DockerContainer.DEFAULT_DOCKER_HOST }
            val uri = URI(trimmed)
            return when (uri.scheme) {
                "unix" -> {
                    require(!uri.path.isNullOrBlank() && uri.path != "/") { "Docker socket path is required (unix:///var/run/docker.sock)" }
                    Unix(Path.of(uri.path))
                }

                "tcp", "http", "https" -> {
                    require(!uri.host.isNullOrBlank()) { "Docker host is required" }
                    val scheme = if (uri.scheme == "https") "https" else "http"
                    val port = if (uri.port == -1 && uri.scheme == "tcp") DEFAULT_TCP_PORT else uri.port
                    Http(URI(scheme, null, uri.host, port, null, null, null))
                }

                else -> {
                    throw IllegalArgumentException("Docker host must start with unix://, tcp://, http:// or https://")
                }
            }
        }
    }
}

data class RawHttpResponse(
    val status: Int,
    val body: ByteArray,
) {
    override fun equals(other: Any?) = other is RawHttpResponse && status == other.status && body.contentEquals(other.body)

    override fun hashCode() = 31 * status + body.contentHashCode()

    companion object {
        private val HEADER_END = "\r\n\r\n".toByteArray(StandardCharsets.US_ASCII)

        fun parse(raw: ByteArray): RawHttpResponse {
            val headerEnd = indexOf(raw, HEADER_END)
            require(headerEnd >= 0) { "invalid HTTP response from Docker Engine" }
            val head = String(raw, 0, headerEnd, StandardCharsets.ISO_8859_1).split("\r\n")
            val status =
                head
                    .first()
                    .split(' ')
                    .getOrNull(1)
                    ?.toIntOrNull()
                    ?: throw IllegalArgumentException("invalid HTTP status line: ${head.first()}")
            val headers =
                head
                    .drop(1)
                    .filter { ':' in it }
                    .associate { it.substringBefore(':').trim().lowercase() to it.substringAfter(':').trim() }
            val rest = raw.copyOfRange(headerEnd + HEADER_END.size, raw.size)

            val body =
                when {
                    headers["transfer-encoding"]?.contains("chunked", ignoreCase = true) == true -> dechunk(rest)
                    headers["content-length"]?.toIntOrNull() != null -> rest.copyOf(minOf(rest.size, headers.getValue("content-length").toInt()))
                    else -> rest
                }
            return RawHttpResponse(status, body)
        }

        private fun dechunk(data: ByteArray): ByteArray {
            val output = ByteArrayOutputStream()
            var position = 0
            while (position < data.size) {
                val lineEnd = indexOf(data, "\r\n".toByteArray(), position)
                require(lineEnd >= 0) { "invalid chunked body" }
                val size = String(data, position, lineEnd - position, StandardCharsets.US_ASCII).substringBefore(';').trim().toInt(16)
                if (size == 0) break
                val start = lineEnd + 2
                require(start + size <= data.size) { "truncated chunked body" }
                output.write(data, start, size)
                position = start + size + 2
            }
            return output.toByteArray()
        }

        private fun indexOf(
            data: ByteArray,
            pattern: ByteArray,
            from: Int = 0,
        ): Int {
            outer@ for (i in from..data.size - pattern.size) {
                for (j in pattern.indices) if (data[i + j] != pattern[j]) continue@outer
                return i
            }
            return -1
        }
    }
}
