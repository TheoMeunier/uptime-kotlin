package tmenier.fr

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Incoming
import tmenier.fr.common.dtos.NotificationJob
import tmenier.fr.common.utils.logger
import tmenier.fr.notifications.NotificationService

@ApplicationScoped
class NotificationWorker(
    private val notificationService: NotificationService,
) {
    @ConfigProperty(name = "scheduler.strategy", defaultValue = "none")
    private lateinit var strategy: String

    @Incoming("notification-jobs-in")
    @ActivateRequestContext
    suspend fun handleNotificationJob(job: NotificationJob) {
        if (strategy != "rabbitmq") return

        try {
            logger.info { "Sending notification for probe ${job.probeId}" }

            notificationService.sendNotification(
                job.probeId,
                job.result,
                job.previousStatus,
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to send notification for probe ${job.probeId}" }
            throw e
        }
    }
}
