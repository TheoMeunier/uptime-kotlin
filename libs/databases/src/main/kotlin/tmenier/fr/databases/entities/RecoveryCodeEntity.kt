package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_recovery_codes")
class RecoveryCodeEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(name = "user_id", nullable = false)
    lateinit var userId: UUID

    @Column(name = "code_hash", nullable = false, length = 64)
    lateinit var codeHash: String

    @Column(name = "used_at")
    var usedAt: Instant? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant
}
