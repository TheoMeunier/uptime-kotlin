package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import tmenier.fr.common.enums.probes.QueueJobStatus
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "probe_check_jobs")
class ProbeCheckTaskEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(name = "probe_id", nullable = false)
    lateinit var probeId: UUID

    @Column(nullable = false)
    lateinit var region: String

    @Column(name = "probe_attempt", nullable = false)
    var attemptNumber: Int = 1

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: QueueJobStatus = QueueJobStatus.PENDING

    @Column(name = "scheduled_at", nullable = false)
    lateinit var scheduledAt: LocalDateTime

    @Column(name = "available_at", nullable = false)
    lateinit var availableAt: Instant

    @Column(name = "lease_owner")
    var claimedBy: String? = null

    @Column(name = "lease_until")
    var leaseUntil: Instant? = null

    @Column(name = "delivery_attempts", nullable = false)
    var deliveryAttempts: Int = 0

    @Column(name = "max_delivery_attempts", nullable = false)
    var maxDeliveryAttempts: Int = 5

    @Column(name = "previous_failed_at")
    var previousFailedAt: Instant? = null

    @Column(name = "last_error")
    var resultMessage: String? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
}
