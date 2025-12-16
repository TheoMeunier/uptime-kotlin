package tmenier.fr.monitors.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.schedulers.ProbeSchedulerInterface
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.net.InetSocketAddress
import java.net.Socket

@ApplicationScoped
class ProbeProtocolTcp: ProbeSchedulerInterface {
    override fun execute(probe: ProbesEntity): ProbeResult {
        val startTime = System.currentTimeMillis()

        try {
            Socket().use { socket ->
                socket.connect(
                    InetSocketAddress(
                        probe.url,
                        probe.tcpPort!!
                    ),
                    5000
                )
                val responseTime = System.currentTimeMillis() - startTime
                return ProbeResult(
                    true, responseTime,
                    "Port TCP " + probe.tcpPort + " ouvert"
                )
            }
        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime
            val result = ProbeResult(
                false, responseTime,
                "Port TCP inaccessible"
            )
            result.error = e.message
            return result
        }
    }

    override fun getProtocolType() = ProbeProtocol.TCP.name
}