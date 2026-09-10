package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "maintenance_occurrences")
class MaintenanceOccurrenceEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "window_id", nullable = false)
    lateinit var window: MaintenanceWindowEntity

    @Column(name = "starts_at", nullable = false)
    lateinit var startsAt: Instant

    @Column(name = "ends_at", nullable = false)
    lateinit var endsAt: Instant

    @Column(nullable = false)
    var cancelled: Boolean = false

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant
}
