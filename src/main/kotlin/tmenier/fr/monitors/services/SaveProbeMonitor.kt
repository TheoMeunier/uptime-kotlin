package tmenier.fr.monitors.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.entities.ProbesMonitorsLogEntity
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.schedulers.dto.ProbeResult
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class SaveProbeMonitor {

    @Transactional
    fun saveProbeMonitorLog(probe: ProbesEntity, runAt: LocalDateTime, result: ProbeResult) {
        val manageProbe = ProbesEntity.findById(probe.id)
            ?: throw IllegalArgumentException("Probe not found with id: ${probe.id}")

        setLastRun(manageProbe, runAt, result.status)

        val monitorLog = ProbesMonitorsLogEntity()
        monitorLog.id = UUID.randomUUID()
        monitorLog.probe = probe
        monitorLog.runAt = runAt
        monitorLog.message = result.message
        monitorLog.status = result.status
        monitorLog.responseTime = result.responseTime
        monitorLog.persist()
    }

    private fun setLastRun(probe: ProbesEntity, runAt: LocalDateTime, status: ProbeMonitorLogStatus) {
        probe.status = status
        probe.lastRun = runAt
        probe.persist()
    }
}