package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.entities.InstanceSettingsEntity

@ApplicationScoped
class InstanceSettingsRepository : PanacheRepositoryBase<InstanceSettingsEntity, Short> {
    fun get(): InstanceSettingsEntity =
        findById(InstanceSettingsEntity.SINGLETON_ID)
            ?: InstanceSettingsEntity().also { it.persist() }

    fun defaultLogRetentionDays(): Int? = findById(InstanceSettingsEntity.SINGLETON_ID)?.logRetentionDays

    fun updateLogRetentionDays(days: Int?): InstanceSettingsEntity {
        val settings = get()
        settings.logRetentionDays = days
        return settings
    }
}
