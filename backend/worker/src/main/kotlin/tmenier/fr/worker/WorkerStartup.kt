package tmenier.fr.worker

import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import tmenier.fr.common.config.SchedulerStrategy
import tmenier.fr.common.utils.logger

/**
 * The worker only exists to run the background jobs of libs/scheduler. Started with
 * scheduler.strategy=none it would stay up and healthy while checking nothing, so say it loudly.
 */
@ApplicationScoped
class WorkerStartup(
    private val schedulerStrategy: SchedulerStrategy,
) {
    fun onStart(
        @Observes event: StartupEvent,
    ) {
        if (!schedulerStrategy.runsBackgroundJobs) {
            logger.warn {
                "This worker is idle (scheduler.strategy=${schedulerStrategy.value}): " +
                    "set SCHEDULER_STRATEGY=database for it to run the Probe Checks"
            }
        }
    }
}
