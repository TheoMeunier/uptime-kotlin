package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.databases.dtos.ProbeOnOffDto
import tmenier.fr.databases.repositories.ProbeRepository
import java.util.UUID

@ApplicationScoped
class OnOffProbeMonitorAction(
    val probeRepository: ProbeRepository
) {
    fun execute(
        probeId: UUID,
        enabled: Boolean,
    ) {
        val dto = ProbeOnOffDto(
            id = probeId,
            enabled = enabled,
            status = if (enabled) ProbeMonitorLogStatus.SUCCESS else ProbeMonitorLogStatus.PAUSE,
        )

        probeRepository.onOff(dto)
    }
}
