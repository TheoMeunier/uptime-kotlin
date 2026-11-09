package tmenier.fr.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.ProbeDTO
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@ApplicationScoped
class ProbeProtocolPing : ProbeProtocolAbstract<ProbeContent.Ping>() {
    override fun execute(
        probe: ProbeDTO,
        content: ProbeContent.Ping,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val start = now()

        return try {
            val cleanUrl = PingTarget.host(content.ip)
            val maxPackets = content.pingMaxPacket
            val delay = content.pingDelay.toLong()

            var successfulPings = 0
            var totalResponseTime = 0.0
            val pingResults = mutableListOf<Boolean>()
            var resolvedAddress: String? = null
            var unresolvedHost = false

            repeat(maxPackets) { iteration ->
                if (iteration > 0) {
                    Thread.sleep(delay)
                }

                val reply = systemPing(cleanUrl, timeoutSeconds(probe))
                pingResults.add(reply.reachable)
                reply.resolvedAddress?.let { resolvedAddress = it }
                if (reply.unresolvedHost) unresolvedHost = true

                if (reply.reachable) {
                    successfulPings++
                    totalResponseTime += reply.rttMillis
                }
            }

            val avgResponseTime =
                if (successfulPings > 0) {
                    (totalResponseTime / successfulPings).roundToLong()
                } else {
                    0L
                }

            val successRate = (successfulPings.toDouble() / maxPackets) * 100

            val status =
                when {
                    successfulPings == maxPackets -> ProbeMonitorLogStatus.SUCCESS
                    successfulPings > 0 -> ProbeMonitorLogStatus.WARNING
                    else -> if (isLastAttempt) ProbeMonitorLogStatus.FAILURE else ProbeMonitorLogStatus.WARNING
                }

            val target = PingTarget.describe(cleanUrl, resolvedAddress)
            val message =
                if (status == ProbeMonitorLogStatus.SUCCESS) {
                    "Ping successful to $target: $successfulPings/$maxPackets packets received - Avg: ${avgResponseTime}ms"
                } else if (successfulPings > 0) {
                    "Ping warning to $target: $successfulPings/$maxPackets packets received (${successRate.toInt()}%) - Avg: ${avgResponseTime}ms"
                } else if (unresolvedHost) {
                    "Ping failed to $cleanUrl: DNS resolution failed - hostname could not be resolved"
                } else {
                    "Ping failed to $target: 0/$maxPackets packets received - Host unreachable or ICMP blocked"
                }

            ProbeResult(
                status = status,
                responseTime = avgResponseTime,
                message = message,
                runAt = start,
            )
        } catch (e: Exception) {
            ProbeResult(
                status = if (isLastAttempt) ProbeMonitorLogStatus.FAILURE else ProbeMonitorLogStatus.WARNING,
                responseTime = getResponseTime(start),
                message = "Ping failed: ${e.message}",
                runAt = start,
            )
        }
    }

    override fun getProtocolType() = ProbeProtocol.PING.name

    private fun systemPing(
        host: String,
        timeoutSeconds: Int,
    ): PingReply {
        val startNanos = System.nanoTime()

        return try {
            val command = listOf("ping", "-c", "1", "-W", waitArgument(timeoutSeconds), host)

            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            processBuilder.environment()["LC_ALL"] = "C"
            val process =
                try {
                    processBuilder.start()
                } catch (e: IOException) {
                    throw PingUnavailableException("ping command is not available on the worker (${e.message})", e)
                }

            val output =
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    reader.readText()
                }

            val finished = process.waitFor(timeoutSeconds.toLong() + 2, TimeUnit.SECONDS)
            val exitCode = if (finished) process.exitValue() else -1

            if (!finished) {
                process.destroyForcibly()
            }

            val wallClockMillis = elapsedMillis(startNanos)
            val success = exitCode == 0

            if (!success) {
                logger.info { "Ping failed for $host. Exit code: $exitCode. Output: ${output.trim()}" }
                return PingReply(
                    reachable = false,
                    rttMillis = wallClockMillis,
                    resolvedAddress = PingOutputParser.resolvedAddress(output),
                    unresolvedHost = PingOutputParser.isUnresolvedHost(output),
                )
            }

            PingReply(
                reachable = true,
                rttMillis = PingOutputParser.rttMillis(output) ?: wallClockMillis,
                resolvedAddress = PingOutputParser.resolvedAddress(output),
            )
        } catch (e: PingUnavailableException) {
            throw e
        } catch (e: Exception) {
            logger.error { "Ping failed for $host: ${e.message}" }
            PingReply(false, elapsedMillis(startNanos))
        }
    }

    private fun waitArgument(timeoutSeconds: Int): String = if (IS_MAC_OS) "${timeoutSeconds * 1000}" else "$timeoutSeconds"

    private fun elapsedMillis(startNanos: Long): Double = (System.nanoTime() - startNanos) / 1_000_000.0

    private companion object {
        val IS_MAC_OS =
            System
                .getProperty("os.name")
                .orEmpty()
                .lowercase()
                .contains("mac")
    }

    private data class PingReply(
        val reachable: Boolean,
        val rttMillis: Double,
        val resolvedAddress: String? = null,
        val unresolvedHost: Boolean = false,
    )
}

internal object PingTarget {
    private val SCHEME = Regex("^[A-Za-z][A-Za-z0-9+.-]*://")

    fun host(target: String): String {
        val authority = target.trim().replaceFirst(SCHEME, "").substringBefore('/')
        if (authority.startsWith("[")) return authority.substringAfter('[').substringBefore(']')
        return if (authority.count { it == ':' } == 1) authority.substringBefore(':') else authority
    }

    fun describe(
        host: String,
        resolvedAddress: String?,
    ): String =
        if (resolvedAddress.isNullOrBlank() || resolvedAddress.equals(host, ignoreCase = true)) {
            host
        } else {
            "$host ($resolvedAddress)"
        }
}

internal object PingOutputParser {
    private val TIME_REGEX = Regex("""time\s*[=<]\s*(\d+(?:[.,]\d+)?)\s*ms""", RegexOption.IGNORE_CASE)
    private val SUMMARY_REGEX = Regex("""min/avg/max\S*\s*=\s*[\d.,]+/([\d.,]+)/""")
    private val RESOLVED_REGEX = Regex("""^PING\b[^\n]*?\(([0-9A-Fa-f.:]+)\)""", RegexOption.MULTILINE)

    private val UNRESOLVED_REGEX =
        Regex(
            "Name or service not known|Temporary failure in name resolution|No address associated with hostname|" +
                "cannot resolve|Unknown host|bad address",
            RegexOption.IGNORE_CASE,
        )

    fun resolvedAddress(output: String): String? = RESOLVED_REGEX.find(output)?.groupValues?.get(1)

    fun isUnresolvedHost(output: String): Boolean = UNRESOLVED_REGEX.containsMatchIn(output)

    fun rttMillis(output: String): Double? =
        (TIME_REGEX.find(output) ?: SUMMARY_REGEX.find(output))
            ?.groupValues
            ?.get(1)
            ?.replace(',', '.')
            ?.toDoubleOrNull()
}

private class PingUnavailableException(
    message: String,
    cause: Throwable,
) : RuntimeException(message, cause)
