package tmenier.fr.schedulers.jobs

import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.AdvisoryLockRepository
import tmenier.fr.databases.repositories.ProbeMonitorRepository
import java.time.LocalDateTime

@ApplicationScoped
class ProbeLogRetentionService(
    private val probeMonitorRepository: ProbeMonitorRepository,
    private val advisoryLockRepository: AdvisoryLockRepository,
) {
    @Scheduled(cron = "0 30 3 * * ?", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun purgeExpiredLogs() {
        try {
            val purged = purge(LocalDateTime.now())
            if (purged > 0) {
                logger.info { "Purged $purged probe log(s) past their retention" }
            }
        } catch (error: Exception) {
            logger.error(error) { "Failed to purge probe logs past their retention" }
        }
    }

    fun purge(
        now: LocalDateTime,
        batchSize: Int = BATCH_SIZE,
        maxPerRun: Int = MAX_PER_RUN,
    ): Int {
        var total = 0
        while (total < maxPerRun) {
            val deleted =
                QuarkusTransaction.requiringNew().call {
                    if (!advisoryLockRepository.tryLock(AdvisoryLockRepository.LOG_RETENTION)) {
                        LOCKED
                    } else {
                        probeMonitorRepository.deleteExpiredBatch(now, minOf(batchSize, maxPerRun - total))
                    }
                }

            if (deleted == LOCKED) {
                logger.debug { "Probe log retention skipped: another worker holds the lock" }
                break
            }

            total += deleted
            if (deleted < batchSize) break
        }
        return total
    }

    companion object {
        const val BATCH_SIZE: Int = 10_000
        const val MAX_PER_RUN: Int = 2_000_000
        private const val LOCKED: Int = -1
    }
}
