package tmenier.fr.monitors.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import org.xbill.DNS.*
import org.xbill.DNS.SimpleResolver
import tmenier.fr.monitors.enums.ProbeProtocol
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.schedulers.ProbeSchedulerInterface
import tmenier.fr.monitors.schedulers.dto.ProbeResult


@ApplicationScoped
class ProbeProtocolDns: ProbeSchedulerInterface {
    override fun execute(probe: ProbesEntity): ProbeResult {
        val startTime = System.currentTimeMillis()

        try {
            val resolver = getResolver(probe)

            val lookup = Lookup(probe.url, Type.A)
            lookup.setResolver(resolver)

            val record = lookup.run()
            val responseTime = System.currentTimeMillis() - startTime

            return ProbeResult(
                true, responseTime,
                "DNS résolu: " + (record?.joinToString { it.rdataToString() } ?: "Aucun enregistrement trouvé")
            )
        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime
            val result = ProbeResult(
                false, responseTime,
                "Échec résolution DNS"
            )
            result.error = e.message
            return result
        }
    }

    private fun getResolver(probe: ProbesEntity): SimpleResolver {
        return SimpleResolver("${probe.dnsServer}:${probe.dnsPort}")
    }

    override fun getProtocolType() = ProbeProtocol.DNS.name
}