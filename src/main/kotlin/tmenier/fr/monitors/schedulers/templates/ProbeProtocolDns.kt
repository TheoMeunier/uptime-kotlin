package tmenier.fr.monitors.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import org.xbill.DNS.Lookup
import org.xbill.DNS.SimpleResolver
import org.xbill.DNS.Type
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.schedulers.dto.ProbeResult

@ApplicationScoped
class ProbeProtocolDns : ProbeProtocolAbstract() {
    override fun execute(probe: ProbesEntity, isLastAttempt: Boolean): ProbeResult {
        val start = now()

        return try {
            val resolver = SimpleResolver(probe.dnsServer).apply {
                port = probe.dnsPort!!
            }

            val lookup = Lookup(probe.url, Type.A)
            lookup.setResolver(resolver)

            val record = lookup.run()

            ProbeResult(
                ProbeMonitorLogStatus.SUCCESS,
                getResponseTime(start),
                "DNS lookup successful: ${record?.size ?: 0} record(s) found in ${getResponseTime(start)} ms",
                getRunAt(start)
            )
        } catch (e: Exception) {
            ProbeResult(
                status = if (isLastAttempt) ProbeMonitorLogStatus.FAILURE else ProbeMonitorLogStatus.WARNING,
                responseTime = getResponseTime(start),
                message = "DNS lookup failed: ${e.message}",
                runAt = getRunAt(start)
            )
        }
    }

    override fun getProtocolType() = ProbeProtocol.DNS.name
}