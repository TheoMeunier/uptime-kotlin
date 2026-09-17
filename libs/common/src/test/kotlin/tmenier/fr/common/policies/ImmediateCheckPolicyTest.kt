package tmenier.fr.common.policies

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.probes.ImmediateCheckOutcome
import tmenier.fr.common.enums.probes.QueueJobStatus

class ImmediateCheckPolicyTest {
    @Test
    fun `a pending job is pulled forward rather than duplicated`() {
        val decision = ImmediateCheckPolicy.decide(probeEnabled = true, activeJobStatus = QueueJobStatus.PENDING)

        assertEquals(ImmediateCheckDecision.PULL_JOB_FORWARD, decision)
        assertEquals(ImmediateCheckOutcome.TRIGGERED, decision.toOutcome())
    }

    @Test
    fun `a probe with no active job has its own schedule pulled forward`() {
        val decision = ImmediateCheckPolicy.decide(probeEnabled = true, activeJobStatus = null)

        assertEquals(ImmediateCheckDecision.PULL_PROBE_SCHEDULE_FORWARD, decision)
        assertEquals(ImmediateCheckOutcome.TRIGGERED, decision.toOutcome())
    }

    @Test
    fun `a dead job does not block a manual check`() {
        val decision = ImmediateCheckPolicy.decide(probeEnabled = true, activeJobStatus = QueueJobStatus.DEAD)

        assertEquals(ImmediateCheckDecision.PULL_PROBE_SCHEDULE_FORWARD, decision)
    }

    @Test
    fun `a leased job is left alone so no second check is fired`() {
        val decision = ImmediateCheckPolicy.decide(probeEnabled = true, activeJobStatus = QueueJobStatus.LEASED)

        assertEquals(ImmediateCheckDecision.DO_NOTHING_ALREADY_RUNNING, decision)
        assertEquals(ImmediateCheckOutcome.ALREADY_RUNNING, decision.toOutcome())
    }

    @Test
    fun `a paused probe is never checked, whatever its queue state`() {
        listOf(null, QueueJobStatus.PENDING, QueueJobStatus.LEASED, QueueJobStatus.DEAD).forEach { status ->
            val decision = ImmediateCheckPolicy.decide(probeEnabled = false, activeJobStatus = status)

            assertEquals(
                ImmediateCheckDecision.DO_NOTHING_DISABLED,
                decision,
                "A paused probe must stay paused, queue status was $status",
            )
            assertEquals(ImmediateCheckOutcome.DISABLED, decision.toOutcome())
        }
    }
}
