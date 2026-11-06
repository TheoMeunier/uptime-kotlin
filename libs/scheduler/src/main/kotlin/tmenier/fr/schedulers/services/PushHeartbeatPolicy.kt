package tmenier.fr.schedulers.services

import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.probes.PushSignal
import tmenier.fr.common.utils.toHumanReadable
import java.time.Duration
import java.time.Instant

object PushHeartbeatPolicy {
    data class Verdict(
        val status: ProbeMonitorLogStatus,
        val message: String,
    )

    fun evaluate(
        now: Instant,
        armedAt: Instant,
        lastPushAt: Instant?,
        lastPushSignal: PushSignal?,
        lastPushMessage: String?,
        periodSeconds: Int,
        graceSeconds: Int,
    ): Verdict {
        val lastPush = lastPushAt?.takeIf { !it.isBefore(armedAt) }
        val detail = lastPushMessage?.takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty()

        if (lastPush != null && lastPushSignal == PushSignal.DOWN) {
            return Verdict(
                ProbeMonitorLogStatus.FAILURE,
                "Job reported a failure ${ago(lastPush, now)} ago$detail",
            )
        }

        val reference = lastPush ?: armedAt
        val deadline = reference.plusSeconds(periodSeconds.toLong() + graceSeconds.toLong())
        val expected = "expected every ${Duration.ofSeconds(periodSeconds.toLong()).toHumanReadable()}"

        if (now.isAfter(deadline)) {
            val message =
                if (lastPush == null) {
                    "No ping received since monitoring started ${ago(armedAt, now)} ago ($expected)"
                } else {
                    "No ping received for ${ago(lastPush, now)} ($expected)"
                }
            return Verdict(ProbeMonitorLogStatus.FAILURE, message)
        }

        val message =
            if (lastPush == null) {
                "Waiting for first ping, deadline in ${Duration.between(now, deadline).toHumanReadable()}"
            } else {
                "Last ping ${ago(lastPush, now)} ago$detail"
            }
        return Verdict(ProbeMonitorLogStatus.SUCCESS, message)
    }

    private fun ago(
        from: Instant,
        now: Instant,
    ): String = Duration.between(from, now).coerceAtLeast(Duration.ZERO).toHumanReadable()
}
