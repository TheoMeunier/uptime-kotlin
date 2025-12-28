package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.NotFoundException
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import java.util.*

@ApplicationScoped
class OnOffProbeMonitorAction {
    fun execute(
        probeId: UUID,
        enabled: Boolean,
    ) {
        val probe = ProbesEntity.findById(probeId) ?: throw NotFoundException("Probe not found")

        probe.enabled = enabled
        probe.status = if (enabled) ProbeMonitorLogStatus.SUCCESS else ProbeMonitorLogStatus.PAUSE
        probe.persist()
    }
}
