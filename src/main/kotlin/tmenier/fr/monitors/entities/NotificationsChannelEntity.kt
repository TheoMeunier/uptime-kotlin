package tmenier.fr.monitors.entities

import com.fasterxml.jackson.databind.JsonNode
import io.quarkus.hibernate.orm.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import tmenier.fr.monitors.enums.NotificationChannelsEnum
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "notifications_channels")
class NotificationsChannelEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(nullable = false, length = 255)
    lateinit var name: String

    @Column(nullable = false)
    var type: NotificationChannelsEnum = NotificationChannelsEnum.DISCORD

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    var content: JsonNode? = null

    @ManyToMany(mappedBy = "notifications", fetch = FetchType.LAZY)
    var probes: MutableSet<ProbesEntity> = mutableSetOf()

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime


    companion object : PanacheCompanion<NotificationsChannelEntity> {

    }
}