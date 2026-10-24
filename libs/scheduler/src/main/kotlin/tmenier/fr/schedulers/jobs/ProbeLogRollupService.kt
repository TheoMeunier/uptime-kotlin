package tmenier.fr.schedulers.jobs

import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.AdvisoryLockRepository
import tmenier.fr.databases.repositories.ProbeLogRollupRepository
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@ApplicationScoped
class ProbeLogRollupService(
    private val rollupRepository: ProbeLogRollupRepository,
    private val advisoryLockRepository: AdvisoryLockRepository,
) {
    @Scheduled(cron = "0 5 * * * ?", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun rollUpHourly() {
        try {
            val days = rollUp(Instant.now())
            if (days > 1) {
                logger.info { "Rolled up $days day(s) of probe logs into hourly summaries" }
            }
        } catch (error: Exception) {
            logger.error(error) { "Failed to roll up probe logs" }
        }
    }

    fun rollUp(
        now: Instant,
        maxDays: Int = MAX_DAYS_PER_RUN,
    ): Int {
        val until = now.truncatedTo(ChronoUnit.HOURS)
        var cursor = QuarkusTransaction.requiringNew().call { startOfRun(until) }
        var chunks = 0

        while (cursor.isBefore(until) && chunks < maxDays) {
            val start = cursor
            val end = minOf(start.plus(CHUNK), until)
            val done =
                QuarkusTransaction.requiringNew().call {
                    if (!advisoryLockRepository.tryLock(AdvisoryLockRepository.LOG_ROLLUP)) {
                        false
                    } else {
                        rollupRepository.rollUp(start, end)
                        rollupRepository.advanceWatermark(end)
                        true
                    }
                }

            if (!done) {
                logger.debug { "Probe log rollup skipped: another worker holds the lock" }
                break
            }
            cursor = end
            chunks++
        }

        if (chunks == 0 && !cursor.isBefore(until)) {
            QuarkusTransaction.requiringNew().call {
                if (advisoryLockRepository.tryLock(AdvisoryLockRepository.LOG_ROLLUP)) {
                    rollupRepository.advanceWatermark(until)
                }
            }
        }
        return chunks
    }

    private fun startOfRun(until: Instant): Instant {
        val watermark = rollupRepository.watermark()
        if (watermark != null) return minOf(watermark.minus(RECOMPUTE_WINDOW), until)

        return rollupRepository.oldestRawRunAt()?.truncatedTo(ChronoUnit.HOURS)?.let { minOf(it, until) } ?: until
    }

    companion object {
        val RECOMPUTE_WINDOW: Duration = Duration.ofHours(3)

        private val CHUNK: Duration = Duration.ofDays(1)

        const val MAX_DAYS_PER_RUN: Int = 92
    }
}
