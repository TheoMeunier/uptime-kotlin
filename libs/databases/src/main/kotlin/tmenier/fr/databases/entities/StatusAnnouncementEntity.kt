package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import tmenier.fr.common.enums.statuspages.AnnouncementSeverity
import tmenier.fr.common.enums.statuspages.AnnouncementState
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "status_announcements")
class StatusAnnouncementEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @Column(nullable = false, length = 255)
    lateinit var title: String

    @Column(columnDefinition = "TEXT")
    var body: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var severity: AnnouncementSeverity = AnnouncementSeverity.INFO

    @Column(name = "starts_at", nullable = false)
    lateinit var startsAt: Instant

    @Column(name = "ends_at")
    var endsAt: Instant? = null

    @Column(name = "show_on_global", nullable = false)
    var showOnGlobal: Boolean = false

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "status_announcement_pages",
        joinColumns = [JoinColumn("announcement_id")],
        inverseJoinColumns = [JoinColumn("status_page_id")],
    )
    var pages: MutableSet<StatusPageEntity> = mutableSetOf()

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: Instant

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant

    fun stateAt(now: Instant): AnnouncementState =
        when {
            startsAt.isAfter(now) -> AnnouncementState.SCHEDULED
            endsAt?.let { !it.isAfter(now) } == true -> AnnouncementState.ENDED
            else -> AnnouncementState.ACTIVE
        }
}
