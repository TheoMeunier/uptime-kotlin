package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import tmenier.fr.common.enums.probes.PushSignal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "probe_push_states")
class ProbePushStateEntity : PanacheEntityBase {
    @Id
    @Column(name = "probe_id", nullable = false)
    lateinit var probeId: UUID

    @Column(nullable = false, length = 64)
    lateinit var token: String

    @Column(name = "armed_at", nullable = false)
    lateinit var armedAt: Instant

    @Column(name = "last_push_at")
    var lastPushAt: Instant? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "last_push_signal", length = 8)
    var lastPushSignal: PushSignal? = null

    @Column(name = "last_push_message", columnDefinition = "TEXT")
    var lastPushMessage: String? = null

    @Column(name = "push_count", nullable = false)
    var pushCount: Long = 0
}
