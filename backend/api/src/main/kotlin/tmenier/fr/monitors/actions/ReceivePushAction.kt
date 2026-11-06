package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.common.enums.probes.PushSignal
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository
import tmenier.fr.databases.repositories.ProbePushStateRepository
import tmenier.fr.databases.repositories.ProbePushStateRepository.Companion.TOKEN_FORMAT
import tmenier.fr.databases.repositories.ProbeRepository
import java.time.Instant

@ApplicationScoped
class ReceivePushAction(
    private val probePushStateRepository: ProbePushStateRepository,
    private val probeRepository: ProbeRepository,
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
) {
    enum class Outcome { ACCEPTED, PAUSED, NOT_FOUND }

    @Transactional
    fun execute(
        token: String,
        signal: PushSignal,
        message: String?,
        now: Instant = Instant.now(),
    ): Outcome {
        if (!TOKEN_FORMAT.matches(token)) return Outcome.NOT_FOUND

        val state = probePushStateRepository.findByTokenForUpdate(token) ?: return Outcome.NOT_FOUND
        val probe = probeRepository.findByIdOrNull(state.probeId)
        if (probe == null || probe.protocol != ProbeProtocol.PUSH) return Outcome.NOT_FOUND
        if (!probe.enabled) return Outcome.PAUSED

        state.lastPushAt = now
        state.lastPushSignal = signal
        state.lastPushMessage = message?.trim()?.take(MAX_MESSAGE_LENGTH)?.ifEmpty { null }
        state.pushCount += 1

        val changesState =
            when (signal) {
                PushSignal.UP -> probe.status == ProbeMonitorLogStatus.FAILURE || probe.status == ProbeMonitorLogStatus.WARNING
                PushSignal.DOWN -> probe.status != ProbeMonitorLogStatus.FAILURE
            }
        if (changesState) {
            logger.info { "Push received for probe ${probe.id} (signal=$signal, status=${probe.status}): checking now" }
            probeCheckTaskRepository.requestImmediateCheck(probe.id, now)
        }

        return Outcome.ACCEPTED
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 500
    }
}
