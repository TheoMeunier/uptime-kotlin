package tmenier.fr.schedulers.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus

class ProbeAttemptPolicyTest {
    @Test
    fun `retry count is additional to the initial attempt`() {
        assertEquals(
            ProbeMonitorLogStatus.WARNING,
            ProbeAttemptPolicy.durableStatus(
                resultStatus = ProbeMonitorLogStatus.WARNING,
                currentStatus = ProbeMonitorLogStatus.SUCCESS,
                attemptNumber = 3,
                retryCount = 3,
            ),
        )
        assertEquals(
            ProbeMonitorLogStatus.FAILURE,
            ProbeAttemptPolicy.durableStatus(
                resultStatus = ProbeMonitorLogStatus.WARNING,
                currentStatus = ProbeMonitorLogStatus.WARNING,
                attemptNumber = 4,
                retryCount = 3,
            ),
        )
    }

    @Test
    fun `probe already in failure does not start retries`() {
        assertEquals(
            ProbeMonitorLogStatus.FAILURE,
            ProbeAttemptPolicy.durableStatus(
                resultStatus = ProbeMonitorLogStatus.WARNING,
                currentStatus = ProbeMonitorLogStatus.FAILURE,
                attemptNumber = 1,
                retryCount = 3,
            ),
        )
    }

    @Test
    fun `success always restores success`() {
        assertEquals(
            ProbeMonitorLogStatus.SUCCESS,
            ProbeAttemptPolicy.durableStatus(
                resultStatus = ProbeMonitorLogStatus.SUCCESS,
                currentStatus = ProbeMonitorLogStatus.WARNING,
                attemptNumber = 2,
                retryCount = 3,
            ),
        )
    }
}
