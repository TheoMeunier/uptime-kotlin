package tmenier.fr.monitors.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.net.InetSocketAddress
import java.net.Socket

@ApplicationScoped
class ProbeProtocolTcp : ProbeProtocolAbstract() {
    override fun execute(
        probe: ProbesEntity,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val start = now()

        return try {
            Socket().use { socket ->
                socket.connect(
                    InetSocketAddress(
                        probe.url,
                        probe.tcpPort!!,
                    ),
                    probe.timeout,
                )
                return ProbeResult(
                    status = ProbeMonitorLogStatus.SUCCESS,
                    responseTime = getResponseTime(start),
                    message = "TCP connection successful in ${getResponseTime(start)}ms",
                    runAt = getRunAt(start),
                )
            }
        } catch (e: Exception) {
            ProbeResult(
                status = getStatus(isLastAttempt, probe),
                responseTime = getResponseTime(start),
                message = "TCP connection failed: ${e.message}",
                runAt = getRunAt(start),
            )
        }
    }

    override fun getProtocolType() = ProbeProtocol.TCP.name

    private fun getStatus(isLastAttempt: Boolean, probe: ProbesEntity): ProbeMonitorLogStatus {
        if (isLastAttempt) return ProbeMonitorLogStatus.FAILURE

        if (probe.status == ProbeMonitorLogStatus.FAILURE) return ProbeMonitorLogStatus.FAILURE

        return ProbeMonitorLogStatus.WARNING
    }
}
