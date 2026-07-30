package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.databases.dtos.StoreProbeDto
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.services.ResolveMonitorContentService
import java.util.UUID

@ApplicationScoped
class StoreProbeAction(
    private val probeRepository: ProbeRepository,
    private val getProbeContentService: ResolveMonitorContentService,
) {
    @Transactional
    fun execute(
        payload: BaseStoreProbeRequest,
        probeId: UUID? = null,
    ) {
        val isUpdate = probeId != null

        val dto =
            StoreProbeDto(
                id = probeId ?: UUID.randomUUID(),
                name = payload.name,
                interval = payload.interval!!,
                intervalRetry = payload.intervalRetry!!,
                retry = payload.retry!!,
                protocol = payload.protocol,
                enabled = payload.enabled == true,
                description = payload.description,
                content = getProbeContentService.resolve(payload),
            )

        if (isUpdate) {
            probeRepository.update(dto, payload.notifications)
        } else {
            probeRepository.save(dto, payload.notifications)
        }
    }
}
