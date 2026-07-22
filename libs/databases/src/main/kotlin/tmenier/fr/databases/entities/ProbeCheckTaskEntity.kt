package tmenier.fr.databases.entities

import ProbeCheckTaskStatusEnum
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "probe_check_tasks")
class ProbeCheckTaskEntity : PanacheEntityBase {

    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(name = "probe_id", nullable = false)
    lateinit var probeId: UUID

    @Column(nullable = false)
    lateinit var region: String

    @Column(name = "attempt_number", nullable = false)
    var attemptNumber: Int = 1

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ProbeCheckTaskStatusEnum = ProbeCheckTaskStatusEnum.PENDING

    @Column(name = "scheduled_at", nullable = false)
    lateinit var scheduledAt: LocalDateTime

    @Column(name = "claimed_by")
    var claimedBy: String? = null

    @Column(name = "claimed_at")
    var claimedAt: Instant? = null

    @Column(name = "result_message")
    var resultMessage: String? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
}
