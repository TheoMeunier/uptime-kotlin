package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.databases.dtos.StoreProbeDto
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.NotificationRepository
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.services.ResolveMonitorContentService
import java.util.UUID

@ApplicationScoped
class StoreProbeAction(
    private val probeRepository: ProbeRepository,
    private val getProbeContentService: ResolveMonitorContentService,
    private val notificationRepository: NotificationRepository,
) {
    @Transactional
    fun execute(
        payload: BaseStoreProbeRequest,
        probeId: UUID? = null,
    ) {
        if (payload.notifications.isEmpty() && !notificationRepository.hasDefault()) {
            throw BadRequestException(
                "At least one notification is required: select a channel, or mark one as default.",
            )
        }

        val isUpdate = probeId != null
        val existingProbe =
            probeId?.let {
                ProbeMapper.toDto(probeRepository.findById(it))
            }

        val dto =
            StoreProbeDto(
                id = probeId ?: UUID.randomUUID(),
                name = payload.name,
                interval = payload.interval!!,
                timeout = payload.timeout,
                intervalRetry = payload.intervalRetry!!,
                retry = payload.retry!!,
                protocol = payload.protocol,
                enabled = payload.enabled == true,
                description = payload.description,
                content = getProbeContentService.resolve(payload, existingProbe),
                alertRepeatSeconds = payload.alertRepeatSeconds,
                logRetentionDays = payload.logRetentionDays,
            )

        if (isUpdate) {
            probeRepository.update(dto, payload.notifications)
        } else {
            probeRepository.save(dto, payload.notifications)
        }
    }
}
