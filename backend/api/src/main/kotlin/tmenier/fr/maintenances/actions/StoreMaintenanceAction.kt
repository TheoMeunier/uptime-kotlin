package tmenier.fr.maintenances.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.databases.dtos.StoreMaintenanceWindowDto
import tmenier.fr.databases.repositories.MaintenanceWindowRepository
import tmenier.fr.maintenances.requests.StoreMaintenanceRequest
import tmenier.fr.schedulers.services.MaintenanceMaterialiser
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@ApplicationScoped
class StoreMaintenanceAction(
    private val maintenanceWindowRepository: MaintenanceWindowRepository,
    private val maintenanceMaterialiser: MaintenanceMaterialiser,
) {
    @ConfigProperty(name = "maintenance.max-duration-hours", defaultValue = "24")
    var maxDurationHours: Int = 24

    @Transactional
    fun execute(
        payload: StoreMaintenanceRequest,
        maintenanceId: UUID? = null,
    ): UUID {
        validate(payload)

        val isUpdate = maintenanceId != null
        val dto =
            StoreMaintenanceWindowDto(
                id = maintenanceId ?: UUID.randomUUID(),
                title = payload.title.trim(),
                description = payload.description?.trim()?.ifEmpty { null },
                startsAt = payload.startsAt,
                durationSeconds = payload.durationSeconds,
                recurrence = payload.recurrence,
                recurrenceUntil = payload.recurrenceUntil,
                timezone = payload.timezone,
                active = payload.active,
                isPublic = payload.isPublic,
            )

        if (isUpdate) {
            maintenanceWindowRepository.update(dto, payload.probeIds)
            maintenanceMaterialiser.rebuildFuture(dto.id, Instant.now())
        } else {
            maintenanceWindowRepository.save(dto, payload.probeIds)
            maintenanceMaterialiser.materialiseWindow(dto.id, Instant.now())
        }

        return dto.id
    }

    private fun validate(payload: StoreMaintenanceRequest) {
        runCatching { ZoneId.of(payload.timezone) }
            .onFailure { throw BadRequestException("Unknown timezone: ${payload.timezone}") }

        val maxDurationSeconds = maxDurationHours * SECONDS_PER_HOUR
        if (payload.durationSeconds > maxDurationSeconds) {
            throw BadRequestException(
                "Duration must be at most $maxDurationHours hours. " +
                    "A maintenance window nobody remembers to close is a monitoring system silently disarmed.",
            )
        }

        payload.recurrenceUntil?.let {
            if (!it.isAfter(payload.startsAt)) {
                throw BadRequestException("Recurrence end must be after the first occurrence")
            }
        }
    }

    companion object {
        private const val SECONDS_PER_HOUR = 3_600
    }
}
