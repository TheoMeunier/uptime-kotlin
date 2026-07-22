package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "worker_heartbeats")
class WorkerHeartbeat : PanacheEntityBase {
    @Id
    lateinit var region: String

    @Column(name = "worker_id", nullable = false)
    lateinit var workerId: String

    @Column(name = "last_seen_at", nullable = false)
    lateinit var lastSeenAt: Instant
}
