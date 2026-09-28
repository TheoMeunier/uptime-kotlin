package tmenier.fr.schedulers.queue

import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.config.SchedulerStrategy
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.ProbeCheckTaskRepository

@ApplicationScoped
class ProbeSchedulerService(
    private val probeCheckTaskRepository: ProbeCheckTaskRepository,
    private val schedulerStrategy: SchedulerStrategy,
) {
    @ConfigProperty(name = "scheduler.worker.name", defaultValue = "default")
    lateinit var region: String

    @PostConstruct
    fun started() {
        logger.info { "Probe scheduler started: strategy=${schedulerStrategy.value}, defaultRegion=$region, pollInterval=1s" }
    }

    @Scheduled(every = "1s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun enqueueDueChecks() {
        if (!schedulerStrategy.runsBackgroundJobs) return

        try {
            val count = probeCheckTaskRepository.ensureScheduledProbeChecks(region)
            if (count > 0) {
                logger.info { "Scheduled $count next Probe Check task(s) in PostgreSQL" }
            }
        } catch (error: Exception) {
            logger.error(error) { "Failed to schedule next Probe Check tasks" }
        }
    }
}
