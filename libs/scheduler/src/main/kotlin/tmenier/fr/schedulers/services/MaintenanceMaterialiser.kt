package tmenier.fr.schedulers.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.entities.MaintenanceWindowEntity
import tmenier.fr.databases.repositories.MaintenanceOccurrenceRepository
import tmenier.fr.databases.repositories.MaintenanceWindowRepository
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@ApplicationScoped
class MaintenanceMaterialiser(
    private val maintenanceWindowRepository: MaintenanceWindowRepository,
    private val maintenanceOccurrenceRepository: MaintenanceOccurrenceRepository,
) {
    @ConfigProperty(name = "maintenance.horizon-days", defaultValue = "90")
    var horizonDays: Int = 90

    @Transactional
    fun materialiseAll(now: Instant = Instant.now()): Int =
        maintenanceWindowRepository.findMaterialisable().sumOf { window ->
            runCatching { materialise(window, now) }
                .onFailure { logger.error(it) { "Failed to materialise maintenance window ${window.id}" } }
                .getOrDefault(0)
        }

    @Transactional
    fun materialiseWindow(
        windowId: UUID,
        now: Instant = Instant.now(),
    ): Int {
        val window = maintenanceWindowRepository.findByIdOrNull(windowId) ?: return 0
        if (!window.active) return 0

        return materialise(window, now)
    }

    @Transactional
    fun rebuildFuture(
        windowId: UUID,
        now: Instant = Instant.now(),
    ): Int {
        maintenanceOccurrenceRepository.deleteNotStarted(windowId, now)

        return materialiseWindow(windowId, now)
    }

    private fun materialise(
        window: MaintenanceWindowEntity,
        now: Instant,
    ): Int {
        val from = now.minusSeconds(window.durationSeconds.toLong())
        val until = now.plusSeconds(horizonDays.toLong() * SECONDS_PER_DAY)

        val slots =
            MaintenanceRecurrencePlanner.plan(
                rule =
                    MaintenanceRecurrencePlanner.Rule(
                        startsAt = window.startsAt,
                        durationSeconds = window.durationSeconds,
                        recurrence = window.recurrence,
                        recurrenceUntil = window.recurrenceUntil,
                        zone = resolveZone(window),
                    ),
                from = from,
                until = until,
            )

        if (slots.isEmpty()) return 0

        val alreadyThere = maintenanceOccurrenceRepository.findMaterialisedStarts(window.id, from, until)
        val missing = slots.filterNot { it.startsAt in alreadyThere }
        if (missing.isEmpty()) return 0

        val inserted =
            maintenanceOccurrenceRepository.materialise(
                window = window,
                slots = missing.map { it.startsAt to it.endsAt },
            )

        logger.info {
            "Materialised $inserted occurrence(s) for maintenance window ${window.id} " +
                "(${window.recurrence}, horizon=${horizonDays}d)"
        }

        return inserted
    }

    private fun resolveZone(window: MaintenanceWindowEntity): ZoneId =
        runCatching { ZoneId.of(window.timezone) }
            .getOrElse {
                logger.warn { "Maintenance window ${window.id} has an unknown timezone '${window.timezone}', falling back to UTC" }
                ZoneId.of("UTC")
            }

    companion object {
        private const val SECONDS_PER_DAY: Long = 86_400
    }
}
