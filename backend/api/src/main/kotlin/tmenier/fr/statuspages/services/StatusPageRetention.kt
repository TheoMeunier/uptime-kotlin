package tmenier.fr.statuspages.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.dtos.LogRetention
import tmenier.fr.databases.repositories.InstanceSettingsRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.util.UUID

@ApplicationScoped
class StatusPageRetention(
    private val probeRepository: ProbeRepository,
    private val instanceSettingsRepository: InstanceSettingsRepository,
) {
    fun shortestDays(probeIds: Collection<UUID>): Int? {
        if (probeIds.isEmpty()) return null

        val defaultDays = instanceSettingsRepository.defaultLogRetentionDays()
        return probeRepository
            .findByIds(probeIds.toList())
            .mapNotNull { LogRetention.effective(it.logRetentionDays, defaultDays) }
            .minOrNull()
    }
}
