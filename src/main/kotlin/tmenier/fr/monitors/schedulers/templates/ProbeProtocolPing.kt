package tmenier.fr.monitors.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.schedulers.ProbeSchedulerInterface
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.net.InetAddress

@ApplicationScoped
class ProbeProtocolPing: ProbeSchedulerInterface {
    override fun execute(probe: ProbesEntity): ProbeResult {
        val startTime = System.currentTimeMillis()

        try {
            val address = InetAddress.getByName(probe.url)
            val reachable = address.isReachable(5000) // timeout 5s
            val responseTime = System.currentTimeMillis() - startTime

            return ProbeResult(
                reachable, responseTime,
                if (reachable) "Host accessible" else "Host inaccessible"
            )

        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime
            val result = ProbeResult(
                false, responseTime,
                "Échec ping"
            )
            result.error = e.message
            return result
        }
    }

    override fun getProtocolType() = ProbeProtocol.PING.name
}