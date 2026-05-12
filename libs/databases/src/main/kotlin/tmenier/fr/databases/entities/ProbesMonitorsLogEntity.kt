package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "probes_monitors_logs")
class ProbesMonitorsLogEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var status: ProbeMonitorLogStatus

    @Column(name = "response_time", nullable = false)
    var responseTime: Long = 0L

    @Column(name = "message", nullable = false)
    lateinit var message: String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "probe_id", nullable = false)
    open lateinit var probe: ProbesEntity

    @Column(name = "run_at", nullable = false, updatable = false)
    lateinit var runAt: LocalDateTime

    companion object : PanacheCompanion<ProbesMonitorsLogEntity> {
        fun countByProbeAndPeriod(
            probeId: UUID,
            from: LocalDateTime,
            to: LocalDateTime,
        ): Long = count("probe.id = ?1 AND runAt >= ?2 AND runAt <= ?3", probeId, from, to)

        fun countSuccessByProbeAndPeriod(
            probeId: UUID,
            from: LocalDateTime,
            to: LocalDateTime,
        ): Long = count("probe.id = ?1 AND status = ?2 AND runAt >= ?3 AND runAt <= ?4", probeId, ProbeMonitorLogStatus.SUCCESS, from, to)
    }
}
