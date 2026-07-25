package tmenier.fr.notifications.jobs

import io.quarkus.scheduler.Scheduled
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.NotificationTaskRepository
import tmenier.fr.notifications.NotificationDispatcher
import java.net.InetAddress
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@ApplicationScoped
class NotificationRetryJob(
    private val notificationTaskRepository: NotificationTaskRepository,
    private val notificationDispatcher: NotificationDispatcher,
) {
    @ConfigProperty(name = "scheduler.strategy", defaultValue = "none")
    lateinit var clusterStrategy: String

    @ConfigProperty(name = "quarkus.scheduler.strategy", defaultValue = "none")
    lateinit var standaloneStrategy: String

    private val workerId = "notification-${InetAddress.getLocalHost().hostName}"
    private val leaseDuration = Duration.ofMinutes(2)
    private val concurrency = 4
    private val activeDeliveries = AtomicInteger()
    private val activeDeliveryIds = ConcurrentHashMap.newKeySet<java.util.UUID>()
    private val executor = Executors.newFixedThreadPool(concurrency)

    @PostConstruct
    fun started() {
        logger.info {
            "Notification worker started: workerId=$workerId, enabled=${enabled()}, " +
                "concurrency=$concurrency, leaseDuration=${leaseDuration.seconds}s"
        }
    }

    @Scheduled(every = "1s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun dispatchDueDeliveries() {
        if (!enabled()) return

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
            activeDeliveryIds.add(id)
            executor.submit {
                try {
                    logger.info { "Starting notification delivery $id" }
                    val delivery = notificationTaskRepository.findByIdWithRelations(id)
                    notificationDispatcher.dispatch(
                        delivery.notification,
                        delivery.probe,
                        delivery.payload,
                        delivery.event,
                    )
                    notificationTaskRepository.markSent(id)
                    logger.info { "Notification delivery $id sent" }
                } catch (error: Exception) {
                    logger.warn(error) { "Notification delivery $id failed" }
                    notificationTaskRepository.markFailedAndReschedule(
                        id = id,
                        errorMessage = error.message ?: "Unknown notification delivery error",
                    )
                } finally {
                    activeDeliveryIds.remove(id)
                    activeDeliveries.decrementAndGet()
                }
            }
        }
    }

    @Scheduled(every = "30s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun maintainLeases() {
        if (!enabled()) return
        notificationTaskRepository.renewLeases(workerId, leaseDuration, activeDeliveryIds)
        notificationTaskRepository.deadLetterExpiredLeases()
    }

    private fun enabled(): Boolean = clusterStrategy == "database" || standaloneStrategy == "db-lock"

    @PreDestroy
    fun close() {
        executor.shutdownNow()
    }
}
