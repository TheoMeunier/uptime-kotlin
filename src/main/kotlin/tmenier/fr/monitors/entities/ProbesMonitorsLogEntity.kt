package tmenier.fr.monitors.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
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

    companion object : PanacheCompanion<ProbesMonitorsLogEntity>
    {
       //
    }
}