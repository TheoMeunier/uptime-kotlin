package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OrderColumn
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "status_page_groups")
class StatusPageGroupEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    lateinit var id: UUID

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_page_id", nullable = false)
    lateinit var statusPage: StatusPageEntity

    @Column(length = 255)
    var name: String? = null

    @Column(nullable = false)
    var position: Int = 0

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "status_page_group_probes",
        joinColumns = [JoinColumn(name = "group_id")],
        inverseJoinColumns = [JoinColumn(name = "probe_id")],
    )
    @OrderColumn(name = "position")
    var probes: MutableList<ProbesEntity> = mutableListOf()

    fun orderedProbes(): List<ProbesEntity> = probes.filterNotNull()
}
