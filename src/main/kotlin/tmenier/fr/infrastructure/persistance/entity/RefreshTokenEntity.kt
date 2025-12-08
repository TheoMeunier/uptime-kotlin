package tmenier.fr.infrastructure.persistance.entity

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_token")
class RefreshTokenEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false, length = 100)
    lateinit var id: UUID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    open lateinit var user: UserEntity

    @Column(name = "refresh_token", nullable = false)
    lateinit var refreshToken: UUID

    @Column(name = "expired_at", nullable = false, updatable = false)
    lateinit var expiredAt: LocalDateTime

    companion object : PanacheCompanion<RefreshTokenEntity> {
        fun findByRefreshToken(refreshToken: UUID): RefreshTokenEntity? {
            return find("refreshToken = ?1", refreshToken).firstResult()
        }
    }
}