package tmenier.fr.settings.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.dtos.LogRetention
import tmenier.fr.databases.dtos.LogRetentionPreviewDto
import tmenier.fr.databases.repositories.InstanceSettingsRepository
import tmenier.fr.databases.repositories.ProbeMonitorRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@ApplicationScoped
class PreviewLogRetentionAction(
    private val probeMonitorRepository: ProbeMonitorRepository,
    private val instanceSettingsRepository: InstanceSettingsRepository,
) {
    fun forInstanceDefault(days: Int?): LogRetentionPreviewDto {
        val effective = LogRetention.effective(null, days)

        return LogRetentionPreviewDto(
            retentionDays = effective,
            logsToDelete =
                effective
                    ?.let { probeMonitorRepository.countOlderThanForInheritingProbes(cutoff(it)) }
                    ?: 0L,
            totalLogs = probeMonitorRepository.countForInheritingProbes(),
            oldestLogAt = probeMonitorRepository.oldestRunAtForInheritingProbes(),
        )
    }

    fun forProbe(
        probeId: UUID,
        overrideDays: Int?,
    ): LogRetentionPreviewDto {
        val effective = LogRetention.effective(overrideDays, instanceSettingsRepository.defaultLogRetentionDays())

        return LogRetentionPreviewDto(
            retentionDays = effective,
            logsToDelete =
                effective
                    ?.let { probeMonitorRepository.countOlderThanForProbe(probeId, cutoff(it)) }
                    ?: 0L,
            totalLogs = probeMonitorRepository.countForProbe(probeId),
            oldestLogAt = probeMonitorRepository.oldestRunAtForProbe(probeId),
        )
    }

    private fun cutoff(days: Int): Instant = Instant.now().minus(days.toLong(), ChronoUnit.DAYS)
}
