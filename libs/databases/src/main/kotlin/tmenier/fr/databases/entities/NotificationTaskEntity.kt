package tmenier.fr.databases.entities

import com.fasterxml.jackson.databind.JsonNode
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
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import tmenier.fr.common.enums.notifications.NotificationEvent
import tmenier.fr.common.enums.probes.QueueJobStatus
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "notification_deliveries")
class NotificationTaskEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(name = "probe_check_job_id")
    var checkTaskId: UUID? = null

    @Column(nullable = false)
    var event: NotificationEvent = NotificationEvent.FAILURE

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: QueueJobStatus = QueueJobStatus.PENDING

    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    lateinit var payload: JsonNode

    @Column(name = "last_error")
    var errorMessage: String? = null

    @Column(name = "delivery_attempts", nullable = false)
    var attemptCount: Int = 0

    @Column(name = "max_delivery_attempts", nullable = false)
    var maxAttempts: Int = 5

    @Column(name = "available_at", nullable = false)
    var nextAttemptAt: Instant = Instant.now()

    @Column(name = "lease_owner", length = 100)
    var claimedBy: String? = null

    @Column(name = "lease_until")
    var leaseUntil: Instant? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_channel_id")
    lateinit var notification: NotificationsChannelEntity

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "probe_id", nullable = false)
    lateinit var probe: ProbesEntity
}
