package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import tmenier.fr.common.enums.maintenances.MaintenanceRecurrence
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "maintenance_windows")
class MaintenanceWindowEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(nullable = false, length = 255)
    lateinit var title: String

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Column(name = "starts_at", nullable = false)
    lateinit var startsAt: Instant

    @Column(name = "duration_s", nullable = false)
    var durationSeconds: Int = 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var recurrence: MaintenanceRecurrence = MaintenanceRecurrence.ONCE

    @Column(name = "recurrence_until")
    var recurrenceUntil: Instant? = null

    @Column(nullable = false, length = 64)
    var timezone: String = "UTC"

    @Column(nullable = false)
    var active: Boolean = true

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = true

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "maintenance_window_probes",
        joinColumns = [JoinColumn("maintenance_window_id")],
        inverseJoinColumns = [JoinColumn("probe_id")],
    )
    var probes: MutableSet<ProbesEntity> = mutableSetOf()

    @OneToMany(mappedBy = "window", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    var occurrences: MutableList<MaintenanceOccurrenceEntity> = mutableListOf()

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant
}
