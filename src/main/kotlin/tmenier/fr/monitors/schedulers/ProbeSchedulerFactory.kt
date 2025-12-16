package tmenier.fr.monitors.schedulers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import tmenier.fr.monitors.enums.ProbeProtocol

@ApplicationScoped
class ProbeSchedulerFactory(
    private val probeProtocols: Instance<ProbeSchedulerInterface>
) {

    fun getProtocol(protocolType: ProbeProtocol): ProbeSchedulerInterface? {
        return probeProtocols.stream()
            .filter { it.getProtocolType() == protocolType.name }
            .findFirst()
            .orElse(null)
    }
}