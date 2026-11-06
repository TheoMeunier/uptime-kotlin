package tmenier.fr.schedulers.templates

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.repositories.ProbePushStateRepository
import tmenier.fr.schedulers.services.PushHeartbeatPolicy

@ApplicationScoped
class ProbeProtocolPush(
    private val probePushStateRepository: ProbePushStateRepository,
) : ProbeProtocolAbstract<ProbeContent.Push>() {
    override fun execute(
        probe: ProbeDTO,
        content: ProbeContent.Push,
        isLastAttempt: Boolean,
    ): ProbeResult {
        val now = now()
        val state =
            probePushStateRepository.findByProbeId(probe.id)
                ?: return ProbeResult(
                    status = ProbeMonitorLogStatus.FAILURE,
                    responseTime = 0,
                    message = "Push URL missing: save the monitor again to generate one",
                    runAt = now,
                )

        val verdict =
            PushHeartbeatPolicy.evaluate(
                now = now,
                armedAt = state.armedAt,
                lastPushAt = state.lastPushAt,
                lastPushSignal = state.lastPushSignal,
                lastPushMessage = state.lastPushMessage,
                periodSeconds = content.periodSeconds,
                graceSeconds = content.graceSeconds,
            )

        return ProbeResult(
            status = verdict.status,
            responseTime = 0,
            message = verdict.message,
            runAt = now,
        )
    }

    override fun getProtocolType() = ProbeProtocol.PUSH.name
}
