package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import tmenier.fr.common.enums.users.UserRole
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false, length = 100)
    lateinit var id: UUID

    @Column(nullable = false, length = 255)
    lateinit var name: String

    @Column(nullable = false, unique = true, length = 255)
    lateinit var email: String

    @Column(nullable = false)
    lateinit var password: String

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    var role: UserRole = UserRole.ADMIN

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @Column(name = "onboarding_completed_at")
    var onboardingCompletedAt: Instant? = null

    @OneToMany(mappedBy = "user", cascade = [CascadeType.REMOVE])
    var refreshToken: MutableList<RefreshTokenEntity> = mutableListOf()
}
