package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import tmenier.fr.common.enums.incidents.IncidentClosedReason
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "incidents")
class IncidentEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "probe_id", nullable = false)
    lateinit var probe: ProbesEntity

    @Column(name = "started_at", nullable = false)
    lateinit var startedAt: Instant

    @Column(name = "confirmed_at", nullable = false)
    lateinit var confirmedAt: Instant

    @Column(name = "ended_at")
    var endedAt: Instant? = null

    @Column(name = "duration_seconds")
    var durationSeconds: Long? = null

    @Column(name = "opening_message", nullable = false, columnDefinition = "TEXT")
    var openingMessage: String = ""

    @Column(name = "opening_check_task_id")
    var openingCheckTaskId: UUID? = null

    @Column(name = "closing_check_task_id")
    var closingCheckTaskId: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "closed_reason", length = 16)
    var closedReason: IncidentClosedReason? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant
}
