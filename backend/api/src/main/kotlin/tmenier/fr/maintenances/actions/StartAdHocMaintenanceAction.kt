package tmenier.fr.maintenances.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.maintenances.MaintenanceRecurrence
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.maintenances.requests.StartAdHocMaintenanceRequest
import tmenier.fr.maintenances.requests.StoreMaintenanceRequest
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class StartAdHocMaintenanceAction(
    private val probeRepository: ProbeRepository,
    private val storeMaintenanceAction: StoreMaintenanceAction,
) {
    @Transactional
    fun execute(
        probeId: UUID,
        payload: StartAdHocMaintenanceRequest,
    ): UUID {
        val probe =
            probeRepository.findByIdOrNull(probeId)
                ?: throw NotFoundException("Probe not found: $probeId")

        val request =
            StoreMaintenanceRequest().apply {
                title = payload.title?.trim()?.ifEmpty { null } ?: "Maintenance — ${probe.name}"
                startsAt = Instant.now()
                durationSeconds = payload.durationMinutes * SECONDS_PER_MINUTE
                recurrence = MaintenanceRecurrence.ONCE
                timezone = "UTC"
                active = true
                isPublic = true
                probeIds = listOf(probeId)
            }

        return storeMaintenanceAction.execute(request)
    }

    companion object {
        private const val SECONDS_PER_MINUTE = 60
    }
}
