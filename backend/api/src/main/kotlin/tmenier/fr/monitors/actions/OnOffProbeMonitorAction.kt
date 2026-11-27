package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.ProbeOnOffDto
import tmenier.fr.databases.repositories.ProbePushStateRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.util.UUID

@ApplicationScoped
class OnOffProbeMonitorAction(
    val probeRepository: ProbeRepository,
    private val probePushStateRepository: ProbePushStateRepository,
) {
    fun execute(
        probeId: UUID,
        enabled: Boolean,
    ) {
        val dto =
            ProbeOnOffDto(
                id = probeId,
                enabled = enabled,
                status = if (enabled) ProbeMonitorLogStatus.SUCCESS else ProbeMonitorLogStatus.PAUSE,
            )

        if (!probeRepository.onOff(dto)) throw NotFoundException(null)

        if (enabled) probePushStateRepository.rearm(probeId)
    }
}
