package tmenier.fr.notifications.jobs

import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.config.SchedulerStrategy
import tmenier.fr.common.utils.logger
import tmenier.fr.common.utils.shutdownGracefully
import tmenier.fr.databases.repositories.NotificationTaskRepository
import tmenier.fr.notifications.NotificationDispatcher
import java.net.InetAddress
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicInteger

@ApplicationScoped
class NotificationRetryJob(
    private val notificationTaskRepository: NotificationTaskRepository,
    private val notificationDispatcher: NotificationDispatcher,
    private val schedulerStrategy: SchedulerStrategy,
) {
    private val workerId = "notification-${InetAddress.getLocalHost().hostName}"
    private val leaseDuration = Duration.ofMinutes(2)

    private val maxDeliveryDuration = Duration.ofSeconds(60)
    private val concurrency = 4
    private val activeDeliveries = AtomicInteger()
    private val watchdog = DeliveryWatchdog(maxDeliveryDuration)
    private val executor = Executors.newFixedThreadPool(concurrency)
    private val shutdownTimeout = Duration.ofSeconds(30)

    @Volatile
    private var stopping = false

    @PostConstruct
    fun started() {
        logger.info {
            "Notification worker started: workerId=$workerId, enabled=${enabled()}, " +
                "concurrency=$concurrency, leaseDuration=${leaseDuration.seconds}s, " +
                "maxDeliveryDuration=${maxDeliveryDuration.seconds}s"
        }
    }

    @Scheduled(every = "1s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun dispatchDueDeliveries() {
        if (!enabled() || stopping) return

        val capacity = concurrency - activeDeliveries.get()
        if (capacity <= 0) return

        val ids =
            try {
                notificationTaskRepository.claimDueTasks(workerId, capacity, leaseDuration)
            } catch (error: Exception) {
                logger.error(error) { "Failed to claim notification deliveries" }
                return
            }

        if (ids.isNotEmpty()) {
            logger.info { "Claimed ${ids.size} notification delivery task(s): ${ids.joinToString()}" }
        }

        ids.forEach { id ->
            activeDeliveries.incrementAndGet()
            try {
                executor.submit {
                    watchdog.started(id)
                    try {
                        logger.info { "Starting notification delivery $id" }
                        val delivery = notificationTaskRepository.findByIdWithRelations(id)
                        notificationDispatcher.dispatch(
                            delivery.notification,
                            delivery.probe,
                            delivery.payload,
                            delivery.event,
                            delivery.reminderIndex,
                            delivery.downtime,
                        )
                        notificationTaskRepository.markSent(id)
                        logger.info { "Notification delivery $id sent" }
                    } catch (error: Exception) {
                        Thread.interrupted()
                        logger.warn(error) { "Notification delivery $id failed" }
                        notificationTaskRepository.markFailedAndReschedule(
                            id = id,
                            errorMessage = error.message ?: "Unknown notification delivery error",
                        )
                    } finally {
                        watchdog.finished(id)
                        activeDeliveries.decrementAndGet()
                    }
                }
            } catch (_: RejectedExecutionException) {
                activeDeliveries.decrementAndGet()
                logger.warn { "Notification delivery $id not started: worker is stopping, lease will expire" }
            }
        }
    }

    @Scheduled(every = "30s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun maintainLeases() {
        if (!enabled()) return
        val overdue = watchdog.interruptOverdue()
        if (overdue.isNotEmpty()) {
            logger.warn {
                "Interrupted ${overdue.size} notification delivery(ies) running for more than " +
                    "${maxDeliveryDuration.seconds}s: ${overdue.joinToString()}"
            }
        }
        notificationTaskRepository.renewLeases(workerId, leaseDuration, watchdog.renewable())
        notificationTaskRepository.deadLetterExpiredLeases()
    }

    private fun enabled(): Boolean = schedulerStrategy.runsBackgroundJobs

    @PreDestroy
    fun close() {
        stopping = true
        logger.info { "Stopping notification worker $workerId with ${activeDeliveries.get()} active delivery(ies)" }
        executor.shutdownGracefully("Notification worker $workerId", shutdownTimeout)
    }
}
