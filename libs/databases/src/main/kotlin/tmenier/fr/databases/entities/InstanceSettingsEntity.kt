package tmenier.fr.databases.entities

import io.quarkus.hibernate.orm.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "instance_settings")
class InstanceSettingsEntity : PanacheEntityBase {
    @Id
    @Column(nullable = false)
    var id: Short = SINGLETON_ID

    @Column(name = "log_retention_days")
    var logRetentionDays: Int? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: Instant

    companion object {
        const val SINGLETON_ID: Short = 1
    }
}
