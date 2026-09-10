package tmenier.fr.maintenances.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.MaintenanceWindowDto
import tmenier.fr.databases.dtos.MaintenanceWindowListDto
import tmenier.fr.databases.mappers.MaintenanceMapper
import tmenier.fr.databases.repositories.MaintenanceOccurrenceRepository
import tmenier.fr.databases.repositories.MaintenanceWindowRepository
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class ListMaintenanceAction(
    private val maintenanceWindowRepository: MaintenanceWindowRepository,
    private val maintenanceOccurrenceRepository: MaintenanceOccurrenceRepository,
) {
    @Transactional
    fun execute(now: Instant = Instant.now()): List<MaintenanceWindowListDto> {
        val windows = maintenanceWindowRepository.getAll()
        if (windows.isEmpty()) return emptyList()

        val occurrences = maintenanceOccurrenceRepository.findCurrentAndNextByWindows(windows.map { it.id }, now)

        return windows.map { window ->
            val (current, next) = occurrences[window.id] ?: (null to null)
            MaintenanceMapper.toListDto(window, current, next)
        }
    }

    @Transactional
    fun show(
        maintenanceId: UUID,
        now: Instant = Instant.now(),
    ): MaintenanceWindowDto {
        val window =
            maintenanceWindowRepository.findByIdOrNull(maintenanceId)
                ?: throw NotFoundException("Maintenance window not found: $maintenanceId")

        return MaintenanceMapper.toDto(
            entity = window,
            occurrences = maintenanceOccurrenceRepository.findUpcoming(maintenanceId, now),
            current = maintenanceOccurrenceRepository.findCurrent(maintenanceId, now),
            next = maintenanceOccurrenceRepository.findNext(maintenanceId, now),
        )
    }

    @Transactional
    fun byProbe(
        probeId: UUID,
        now: Instant = Instant.now(),
    ): List<MaintenanceWindowListDto> {
        val windows = maintenanceWindowRepository.findByProbe(probeId)
        if (windows.isEmpty()) return emptyList()

        val occurrences = maintenanceOccurrenceRepository.findCurrentAndNextByWindows(windows.map { it.id }, now)

        return windows.map { window ->
            val (current, next) = occurrences[window.id] ?: (null to null)
            MaintenanceMapper.toListDto(window, current, next)
        }
    }
}
