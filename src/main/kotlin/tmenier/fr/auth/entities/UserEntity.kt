package tmenier.fr.auth.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @OneToMany(mappedBy = "user", cascade = [CascadeType.REMOVE])
    open var refreshToken: MutableList<RefreshTokenEntity> = mutableListOf()

    companion object : PanacheCompanion<UserEntity> {
        fun findByEmail(email: String): UserEntity? = find("email", email).firstResult()

        fun findById(id: UUID): UserEntity? = find("id = ?1", id).firstResult()
    }
}
