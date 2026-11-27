package tmenier.fr.schedulers

import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.enterprise.inject.Instance
import tmenier.fr.common.enums.monitors.ProbeProtocol

@ApplicationScoped
class ProbeSchedulerFactory(
    probeProtocols: Instance<ProbeSchedulerInterface>,
) {
    private val handlers: Map<ProbeProtocol, ProbeSchedulerInterface> = index(probeProtocols.toList())

    fun handlerFor(protocol: ProbeProtocol): ProbeSchedulerInterface = handlers.getValue(protocol)

    fun checkHandlersOnStartup(
        @Observes event: StartupEvent,
    ) = Unit

    companion object {
        internal fun index(handlers: List<ProbeSchedulerInterface>): Map<ProbeProtocol, ProbeSchedulerInterface> {
            val byProtocol = handlers.groupBy { it.protocol }

            val duplicated = byProtocol.filterValues { it.size > 1 }.keys
            check(duplicated.isEmpty()) { "Several probe handlers for: ${duplicated.joinToString()}" }

            val missing = ProbeProtocol.entries - byProtocol.keys
            check(missing.isEmpty()) { "No probe handler for: ${missing.joinToString()}" }

            return byProtocol.mapValues { (_, protocolHandlers) -> protocolHandlers.single() }
        }
    }
}
