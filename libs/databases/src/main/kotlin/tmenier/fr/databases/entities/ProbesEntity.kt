package tmenier.fr.databases.entities

import com.fasterxml.jackson.databind.JsonNode
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "probes")
class ProbesEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(nullable = false, length = 255)
    lateinit var name: String

    @Column(nullable = false)
    var interval: Int = 0

    @Column(nullable = false)
    var timeout: Int = 0

    @Column(name = "retry", nullable = false)
    var retry: Int = 0

    @Column(name = "interval_retry", nullable = false)
    var intervalRetry: Int = 0

    @Column(nullable = false)
    var enabled: Boolean = false

    @Column(nullable = false)
    var status: ProbeMonitorLogStatus = ProbeMonitorLogStatus.FAILURE

    @Column(nullable = false)
    var protocol: ProbeProtocol = ProbeProtocol.HTTP

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "last_run")
    var lastRun: LocalDateTime? = null

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    var content: JsonNode? = null

    @Column(name = "regions_order", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    var regionsOrder: JsonNode? = null

    @Column(name = "next_check_at")
    var nextCheckAt: LocalDateTime? = null

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.REMOVE])
    @JoinTable(
        name = "probes_notifications_channels",
        joinColumns = [JoinColumn("probe_id")],
        inverseJoinColumns = [JoinColumn("notification_channel_id")],
    )
    var notifications: MutableSet<NotificationsChannelEntity> = mutableSetOf()

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    @OneToMany(mappedBy = "probe", cascade = [CascadeType.REMOVE])
    var probesMonitorLogs: MutableList<ProbesMonitorsLogEntity> = mutableListOf()
}
