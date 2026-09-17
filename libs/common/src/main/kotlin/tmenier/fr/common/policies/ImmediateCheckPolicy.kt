package tmenier.fr.common.policies

import tmenier.fr.common.enums.probes.ImmediateCheckOutcome
import tmenier.fr.common.enums.probes.QueueJobStatus

enum class ImmediateCheckDecision {
    PULL_JOB_FORWARD,
    PULL_PROBE_SCHEDULE_FORWARD,
    DO_NOTHING_ALREADY_RUNNING,
    DO_NOTHING_DISABLED,
    ;

    fun toOutcome(): ImmediateCheckOutcome =
        when (this) {
            PULL_JOB_FORWARD, PULL_PROBE_SCHEDULE_FORWARD -> ImmediateCheckOutcome.TRIGGERED
            DO_NOTHING_ALREADY_RUNNING -> ImmediateCheckOutcome.ALREADY_RUNNING
            DO_NOTHING_DISABLED -> ImmediateCheckOutcome.DISABLED
        }
}

object ImmediateCheckPolicy {
    fun decide(
        probeEnabled: Boolean,
        activeJobStatus: QueueJobStatus?,
    ): ImmediateCheckDecision =
        when {
            !probeEnabled -> ImmediateCheckDecision.DO_NOTHING_DISABLED
            activeJobStatus == QueueJobStatus.LEASED -> ImmediateCheckDecision.DO_NOTHING_ALREADY_RUNNING
            activeJobStatus == QueueJobStatus.PENDING -> ImmediateCheckDecision.PULL_JOB_FORWARD
            else -> ImmediateCheckDecision.PULL_PROBE_SCHEDULE_FORWARD
        }
}
