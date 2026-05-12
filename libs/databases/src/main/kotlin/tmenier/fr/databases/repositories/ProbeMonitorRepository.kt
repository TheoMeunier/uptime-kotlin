package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.entities.ProbesMonitorsLogEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class ProbeMonitorRepository {

    fun countByProbeAndPeriod(probeId: UUID, from: LocalDateTime, to: LocalDateTime): Long {
        return ProbesMonitorsLogEntity.countByProbeAndPeriod(probeId, from, to)
    }

    fun countSuccessByProbeAndPeriod(probeId: UUID, from: LocalDateTime, to: LocalDateTime): Long =
        ProbesMonitorsLogEntity.countSuccessByProbeAndPeriod(probeId, from, to)
}
