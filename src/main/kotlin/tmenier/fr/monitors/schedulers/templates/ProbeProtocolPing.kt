package tmenier.fr.monitors.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.logger
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

@ApplicationScoped
class ProbeProtocolPing : ProbeProtocolAbstract() {
    override fun execute(probe: ProbesEntity, isFailed: Boolean?): ProbeResult {
        val start = now()

        return try {
            val cleanUrl = parseUrl(probe.url)
            val maxPackets = probe.pingMaxPacket ?: 3
            val delay = probe.pingDelay?.toLong() ?: 1000L

            var successfulPings = 0
            var totalResponseTime = 0L
            val pingResults = mutableListOf<Boolean>()

            repeat(maxPackets) { iteration ->
                if (iteration > 0) {
                    Thread.sleep(delay)
                }

                val (reachable, pingTime) = systemPing(cleanUrl, 5)
                pingResults.add(reachable)

                if (reachable) {
                    successfulPings++
                    totalResponseTime += pingTime
                }
            }

            val avgResponseTime = if (successfulPings > 0) {
                totalResponseTime / successfulPings
            } else {
                0L
            }

            val successRate = (successfulPings.toDouble() / maxPackets) * 100

            val status = when {
                successfulPings == maxPackets -> ProbeMonitorLogStatus.SUCCESS
                successfulPings > 0 -> ProbeMonitorLogStatus.WARNING
                else -> if (isFailed == true) ProbeMonitorLogStatus.FAILURE else ProbeMonitorLogStatus.WARNING
            }

            val message = if (status == ProbeMonitorLogStatus.SUCCESS) {
                "Ping successful to ${probe.url}: ${successfulPings}/${maxPackets} packets received - Avg: ${avgResponseTime}ms"
            } else if (successfulPings > 0) {
                "Ping warning to ${probe.url}: ${successfulPings}/${maxPackets} packets received (${successRate.toInt()}%) - Avg: ${avgResponseTime}ms"
            } else {
                "Ping failed to ${probe.url}: 0/${maxPackets} packets received - Host unreachable or ICMP blocked"
            }

            ProbeResult(
                status = status,
                responseTime = avgResponseTime,
                message = message,
                runAt = getRunAt(start)
            )

        } catch (e: Exception) {
            ProbeResult(
                status = if (isFailed == true) ProbeMonitorLogStatus.FAILURE else ProbeMonitorLogStatus.WARNING,
                responseTime = getResponseTime(start),
                message = "Ping failed: ${e.message}",
                runAt = getRunAt(start)
            )
        }
    }

    override fun getProtocolType() = ProbeProtocol.PING.name

    private fun parseUrl(url: String): String {
        return url
            .replace("http://", "")
            .replace("https://", "")
            .split("/")[0]
            .split(":")[0]
    }

    private fun systemPing(host: String, timeoutSeconds: Int = 5): Pair<Boolean, Long> {
        val startTime = System.currentTimeMillis()

        return try {
            val command = listOf("ping", "-c", "1", "-W", "$timeoutSeconds", host)

            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()

            val output = StringBuilder()
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
            }

            val finished = process.waitFor(timeoutSeconds.toLong() + 2, TimeUnit.SECONDS)
            val exitCode = if (finished) process.exitValue() else -1

            if (!finished) {
                process.destroy()
            }

            val responseTime = System.currentTimeMillis() - startTime
            val success = exitCode == 0

            if (!success) {
                logger.debug("Ping failed for $host. Exit code: $exitCode. Output: ${output.toString().trim()}")
            }

            Pair(success, responseTime)

        } catch (e: Exception) {
            logger.error("System ping execution failed for $host", e)
            val responseTime = System.currentTimeMillis() - startTime
            Pair(false, responseTime)
        }
    }
}