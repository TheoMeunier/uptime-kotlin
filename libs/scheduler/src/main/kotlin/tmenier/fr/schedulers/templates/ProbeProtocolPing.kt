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
            val cleanUrl = parseUrl(content.ip)
            val maxPackets = content.pingMaxPacket
            val delay = content.pingDelay.toLong()

            var successfulPings = 0
            var totalResponseTime = 0.0
            val pingResults = mutableListOf<Boolean>()

            repeat(maxPackets) { iteration ->
                if (iteration > 0) {
                    Thread.sleep(delay)
                }

                val reply = systemPing(cleanUrl, timeoutSeconds(probe))
                pingResults.add(reply.reachable)

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

            val message =
                if (status == ProbeMonitorLogStatus.SUCCESS) {
                    "Ping successful to $cleanUrl: $successfulPings/$maxPackets packets received - Avg: ${avgResponseTime}ms"
                } else if (successfulPings > 0) {
                    "Ping warning to $cleanUrl: $successfulPings/$maxPackets packets received (${successRate.toInt()}%) - Avg: ${avgResponseTime}ms"
                } else {
                    "Ping failed to $cleanUrl: 0/$maxPackets packets received - Host unreachable or ICMP blocked"
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

    private fun parseUrl(url: String): String =
        url
            .replace("http://", "")
            .replace("https://", "")
            .split("/")[0]
            .split(":")[0]

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
                return PingReply(false, wallClockMillis)
            }

            PingReply(true, PingOutputParser.rttMillis(output) ?: wallClockMillis)
        } catch (e: PingUnavailableException) {
            throw e
        } catch (e: Exception) {
            logger.error { "Ping failed for $host: ${e.message}" }
            PingReply(false, elapsedMillis(startNanos))
        }
    }

    /** `-W` est en secondes sous Linux (iputils, BusyBox) mais en millisecondes sous macOS/BSD. */
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
    )
}

internal object PingOutputParser {
    private val TIME_REGEX = Regex("""time\s*[=<]\s*(\d+(?:[.,]\d+)?)\s*ms""", RegexOption.IGNORE_CASE)

    /** Ligne de résumé : `rtt min/avg/max/mdev = 1/2/3/4 ms` (Linux) ou `round-trip min/avg/max/stddev = …` (macOS, BusyBox). */
    private val SUMMARY_REGEX = Regex("""min/avg/max\S*\s*=\s*[\d.,]+/([\d.,]+)/""")

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
