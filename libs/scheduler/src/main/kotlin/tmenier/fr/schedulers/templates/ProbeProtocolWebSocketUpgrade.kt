package tmenier.fr.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.ProbeHttpClientFactory
import java.net.URI
import java.net.http.WebSocket
import java.net.http.WebSocketHandshakeException
import java.time.Duration
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@ApplicationScoped
class ProbeProtocolWebSocketUpgrade(
    private val httpClientFactory: ProbeHttpClientFactory,
) : ProbeProtocolAbstract<ProbeContent.WebSocketUpgrade>() {
    override fun execute(
        probe: ProbeDTO,
        content: ProbeContent.WebSocketUpgrade,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val start = now()
        val timeout = timeoutOf(probe)

        return try {
            val uri = parseUrl(content.url)
            val subprotocol = content.subprotocol?.trim()?.takeIf { it.isNotEmpty() }

            httpClientFactory.create(false, content.ignoreCertificateErrors, timeout).use { client ->
                val builder = client.newWebSocketBuilder().connectTimeout(timeout)
                subprotocol?.let { builder.subprotocols(it) }

                val webSocket = await(builder.buildAsync(uri, IgnoringListener), timeout)
                val responseTime = getResponseTime(start)
                try {
                    require(subprotocol == null || webSocket.subprotocol == subprotocol) {
                        "server did not accept the subprotocol \"$subprotocol\""
                    }
                } finally {
                    closeQuietly(webSocket)
                }

                ProbeResult(
                    status = ProbeMonitorLogStatus.SUCCESS,
                    responseTime = responseTime,
                    message = "WebSocket upgrade successful (101 Switching Protocols) in ${responseTime}ms",
                    runAt = start,
                    statusCode = SWITCHING_PROTOCOLS,
                )
            }
        } catch (error: Exception) {
            val cause = unwrap(error)
            ProbeResult(
                status = failureStatus(isLastAttempt, probe),
                responseTime = getResponseTime(start),
                message = "WebSocket upgrade failed: ${describe(cause, timeout)}",
                runAt = start,
                statusCode = (cause as? WebSocketHandshakeException)?.response?.statusCode(),
            )
        }
    }

    override fun getProtocolType() = ProbeProtocol.WEBSOCKET.name

    internal fun parseUrl(url: String): URI {
        val uri = URI(url.trim())
        require(uri.scheme == "ws" || uri.scheme == "wss") { "URL must start with ws:// or wss://" }
        require(!uri.host.isNullOrBlank()) { "WebSocket host is required" }
        return uri
    }

    private fun await(
        future: java.util.concurrent.CompletableFuture<WebSocket>,
        timeout: Duration,
    ): WebSocket =
        try {
            future.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
        } catch (error: TimeoutException) {
            future.cancel(true)
            throw error
        }

    private fun closeQuietly(webSocket: WebSocket) {
        try {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "").get(CLOSE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        } catch (_: Exception) {
            // The upgrade already succeeded: a failed close frame does not change the result.
        } finally {
            webSocket.abort()
        }
    }

    private fun unwrap(error: Throwable): Throwable {
        var current = error
        while ((current is ExecutionException || current is CompletionException) && current.cause != null) {
            current = current.cause!!
        }
        return current
    }

    private fun describe(
        error: Throwable,
        timeout: Duration,
    ): String =
        when (error) {
            is WebSocketHandshakeException -> {
                "server answered HTTP ${error.response.statusCode()} instead of 101 Switching Protocols"
            }

            is TimeoutException -> {
                "timed out after ${timeout.toSeconds()}s"
            }

            else -> {
                error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
            }
        }

    private fun failureStatus(
        isLastAttempt: Boolean,
        probe: ProbeDTO,
    ): ProbeMonitorLogStatus =
        if (isLastAttempt || probe.status == ProbeMonitorLogStatus.FAILURE) {
            ProbeMonitorLogStatus.FAILURE
        } else {
            ProbeMonitorLogStatus.WARNING
        }

    private object IgnoringListener : WebSocket.Listener

    private companion object {
        const val SWITCHING_PROTOCOLS = 101
        const val CLOSE_TIMEOUT_MS = 1_000L
    }
}
