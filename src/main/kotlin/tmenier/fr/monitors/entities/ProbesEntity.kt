package tmenier.fr.monitors.entities

import com.fasterxml.jackson.databind.JsonNode
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import io.quarkus.panache.common.Sort
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
import tmenier.fr.monitors.enums.ProbeMonitorLogStatus
import tmenier.fr.monitors.enums.ProbeProtocol
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

    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.REMOVE])
    @JoinTable(
        name = "probes_notifications_channels",
        joinColumns = [JoinColumn("probe_id")],
        inverseJoinColumns = [JoinColumn("notification_channel_id")],
    )
    open var notifications: MutableSet<NotificationsChannelEntity> = mutableSetOf()

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    @OneToMany(mappedBy = "probe", cascade = [CascadeType.REMOVE])
    open var probesMonitorLogs: MutableList<ProbesMonitorsLogEntity> = mutableListOf()

    companion object : PanacheCompanion<ProbesEntity> {
        fun findById(id: UUID): ProbesEntity? = find("id = ?1", id).firstResult()

        fun findByIdWithLogs(
            id: UUID,
            hour: Long = 1,
        ): ProbesEntity? =
            find(
                "SELECT DISTINCT p FROM ProbesEntity p JOIN FETCH p.probesMonitorLogs pml WHERE p.id = ?1 AND pml.runAt > ?2 ORDER BY pml.runAt ASC",
                id,
                LocalDateTime.now().minusHours(hour),
            ).firstResult()

        fun getActiveProbes(): List<ProbesEntity> = find("enabled = ?1 ORDER BY name ASC", true).list()

        fun getAllProbes(): List<ProbesEntity> = findAll(Sort.by("name")).list()

        fun getProbesLastHour(): List<ProbesEntity> =
            find(
                "SELECT DISTINCT p FROM ProbesEntity p JOIN FETCH p.probesMonitorLogs pml WHERE pml.runAt > ?1 AND p.enabled = true ORDER BY pml.runAt ASC",
                LocalDateTime.now().minusHours(1),
            ).list()

        fun delete(id: UUID): Long = delete("id = ?1", id)
    }
}
