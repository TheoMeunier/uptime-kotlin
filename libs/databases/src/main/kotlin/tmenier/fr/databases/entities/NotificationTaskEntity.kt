package tmenier.fr.databases.entities

import ProbeCheckTaskStatusEnum
import com.fasterxml.jackson.databind.JsonNode
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum
import tmenier.fr.common.enums.notifications.NotificationEvent
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "notification_tasks")
class NotificationTaskEntity : PanacheEntityBase {

    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(name = "check_task_id")
    var checkTaskId: UUID? = null

    @Column(nullable = false)
    var event: NotificationEvent = NotificationEvent.FAILURE

    @Column(nullable = false)
    lateinit var channel: NotificationChannelsEnum

    @Column(nullable = false, length = 20)
    var status: ProbeCheckTaskStatusEnum = ProbeCheckTaskStatusEnum.PENDING

    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    lateinit var payload: JsonNode

    @Column(name = "error_message")
    var errorMessage: String? = null

    @Column(name = "attempt_count", nullable = false)
    var attemptCount: Int = 0

    @Column(name = "max_attempts", nullable = false)
    var maxAttempts: Int = 5

    @Column(name = "next_attempt_at", nullable = false)
    var nextAttemptAt: Instant = Instant.now()

    @Column(name = "claimed_by", length = 100)
    var claimedBy: String? = null

    @Column(name = "claimed_at")
    var claimedAt: Instant? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    lateinit var notification: NotificationsChannelEntity

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "probe_id", nullable = false)
    lateinit var probe: ProbesEntity
}
