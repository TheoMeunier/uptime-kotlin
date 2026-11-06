package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.dtos.StoreProbeDto
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.NotificationRepository
import tmenier.fr.databases.repositories.ProbePushStateRepository
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPushRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPushRequest.Companion.PUSH_EVALUATION_INTERVAL_SECONDS
import tmenier.fr.monitors.services.ResolveMonitorContentService
import java.util.UUID

@ApplicationScoped
class StoreProbeAction(
    private val probeRepository: ProbeRepository,
    private val getProbeContentService: ResolveMonitorContentService,
    private val notificationRepository: NotificationRepository,
    private val probePushStateRepository: ProbePushStateRepository,
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

        val isPush = payload is ValidProbeProtocolPushRequest
        val dto =
            StoreProbeDto(
                id = probeId ?: UUID.randomUUID(),
                name = payload.name,
                interval = if (isPush) PUSH_EVALUATION_INTERVAL_SECONDS else payload.interval!!,
                timeout = if (isPush) PUSH_TIMEOUT_SECONDS else payload.timeout,
                intervalRetry = if (isPush) PUSH_EVALUATION_INTERVAL_SECONDS else payload.intervalRetry!!,
                retry = if (isPush) 0 else payload.retry!!,
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

        syncPushState(dto, existingProbe)
    }

    private fun syncPushState(
        dto: StoreProbeDto,
        existingProbe: ProbeDTO?,
    ) {
        if (dto.protocol != ProbeProtocol.PUSH) {
            if (existingProbe?.protocol == ProbeProtocol.PUSH) probePushStateRepository.deleteFor(dto.id)
            return
        }

        probePushStateRepository.ensureFor(dto.id)

        val becameActive = existingProbe != null && dto.enabled && !existingProbe.enabled
        val becamePush = existingProbe != null && existingProbe.protocol != ProbeProtocol.PUSH
        if (becameActive || becamePush) probePushStateRepository.rearm(dto.id)
    }

    companion object {
        private const val PUSH_TIMEOUT_SECONDS = 5
    }
}
