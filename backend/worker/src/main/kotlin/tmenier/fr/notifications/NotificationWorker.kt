package tmenier.fr.notifications

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.RetryDecisionDto
import tmenier.fr.databases.dtos.UpdateNotificationQueueDto
import tmenier.fr.databases.repositories.NotificationTaskRepository
import java.net.InetAddress
import java.time.Instant
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors

@ApplicationScoped
class NotificationRetryWorker(
    private val notificationTaskRepository: NotificationTaskRepository,
    private val notificationDispatcher: NotificationDispatcher
) {
    private val workerId = "notif-${InetAddress.getLocalHost().hostName}"
    private val BASE_BACKOFF_SECONDS = 10L

    private val jobScope =
        CoroutineScope(
            Executors.newFixedThreadPool(2).asCoroutineDispatcher() +
                CoroutineName("NotificationRetryJob") +
                SupervisorJob(),
        )

    @Scheduled(every = "5s")
    fun execute() {
        val ids = try {
            notificationTaskRepository.claimDueTasks(workerId, limit = 20)
        } catch (e: Exception) {
            logger.error(e) { "Failed to claim notification retries" }
            return
        }

        ids.forEach { id ->
            jobScope.launch {
                val info = notificationTaskRepository.findByIdWithRelations(id)

                try {
                    notificationDispatcher.dispatch(
                        info.notification,
                        info.probe,
                        info.payload,
                        info.event,
                        info.taskId
                    )

                    notificationTaskRepository.markSent(id)

                    logger.info { "Notification $id sent on retry" }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.warn(e) { "Retry failed for notification $id" }

                    val decision = retryDecision(info.attemptCount, info.maxAttempts)

                    notificationTaskRepository.markFailedAndReschedule(
                        UpdateNotificationQueueDto(
                            id = id,
                            status = decision.status,
                            errorMessage = e.message ?: "Unknown error",
                            attemptCount = decision.attemptCount,
                            nextAttemptAt = decision.nextAttemptAt,
                            maxAttempts = info.maxAttempts,
                        )
                    )
                }
            }
        }
    }

    private fun retryDecision(attemptCount: Int, maxAttempts: Int): RetryDecisionDto {

        val newAttempt = attemptCount + 1

        return if (newAttempt >= maxAttempts) {
            RetryDecisionDto(status = "failed", attemptCount = newAttempt, nextAttemptAt = null)
        } else {
            val backoffSeconds = BASE_BACKOFF_SECONDS * (1L shl newAttempt)
            RetryDecisionDto(
                status = "pending",
                attemptCount = newAttempt,
                nextAttemptAt = Instant.now().plusSeconds(backoffSeconds)
            )
        }
    }
}
