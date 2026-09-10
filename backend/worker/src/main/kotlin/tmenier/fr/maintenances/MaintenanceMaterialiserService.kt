package tmenier.fr.maintenances

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.AdvisoryLockRepository
import tmenier.fr.schedulers.services.MaintenanceMaterialiser
import java.time.Instant

@ApplicationScoped
class MaintenanceMaterialiserService(
    private val maintenanceMaterialiser: MaintenanceMaterialiser,
    private val advisoryLockRepository: AdvisoryLockRepository,
) {
    @Scheduled(cron = "0 10 * * * ?", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    @Transactional
    fun materialise() {
        if (!advisoryLockRepository.tryLock(AdvisoryLockRepository.MAINTENANCE_MATERIALISER)) {
            logger.debug { "Maintenance materialisation skipped: another worker holds the lock" }
            return
        }

        try {
            val inserted = maintenanceMaterialiser.materialiseAll(Instant.now())
            if (inserted > 0) {
                logger.info { "Materialised $inserted maintenance occurrence(s)" }
            }
        } catch (error: Exception) {
            logger.error(error) { "Failed to materialise maintenance occurrences" }
        }
    }
}
