package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.NotFoundException
import tmenier.fr.common.enums.probes.ImmediateCheckOutcome
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.util.UUID

@ApplicationScoped
class CheckProbeNowAction(
    private val probeRepository: ProbeRepository,
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
) {
    fun execute(probeId: UUID): ImmediateCheckOutcome {
        probeRepository.findByIdOrNull(probeId) ?: throw NotFoundException("Probe not found")

        return probeCheckTaskRepository.requestImmediateCheck(probeId)
    }
}
