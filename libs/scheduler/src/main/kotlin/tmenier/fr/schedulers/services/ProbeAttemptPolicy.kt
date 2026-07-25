package tmenier.fr.schedulers.services

import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus

object ProbeAttemptPolicy {
    fun durableStatus(
        resultStatus: ProbeMonitorLogStatus,
        currentStatus: ProbeMonitorLogStatus,
        attemptNumber: Int,
        retryCount: Int,
    ): ProbeMonitorLogStatus {
        val unsuccessful =
            resultStatus == ProbeMonitorLogStatus.WARNING ||
                resultStatus == ProbeMonitorLogStatus.FAILURE

        return when {
            !unsuccessful -> ProbeMonitorLogStatus.SUCCESS
            currentStatus == ProbeMonitorLogStatus.FAILURE -> ProbeMonitorLogStatus.FAILURE
            attemptNumber >= retryCount + 1 -> ProbeMonitorLogStatus.FAILURE
            else -> ProbeMonitorLogStatus.WARNING
        }
    }
}
