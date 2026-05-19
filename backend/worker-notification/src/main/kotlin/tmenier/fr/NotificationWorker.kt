package tmenier.fr

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.buffer.Buffer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Message
import tmenier.fr.common.dtos.NotificationJob
import tmenier.fr.common.utils.logger
import tmenier.fr.notifications.NotificationService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@ApplicationScoped
class NotificationWorker(
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper,
) {
    @ConfigProperty(name = "scheduler.strategy", defaultValue = "none")
    private lateinit var strategy: String

    private val workerScope =
        CoroutineScope(
            Dispatchers.IO + SupervisorJob(),
        )

    @Incoming("notification-jobs-in")
    @ActivateRequestContext
    fun handleNotificationJob(message: Message<Buffer>): CompletionStage<Void> {
        if (strategy != "rabbitmq") return message.ack()

        val future = CompletableFuture<Void>()

        workerScope.launch {
            try {
                val job = objectMapper.readValue(message.payload.bytes, NotificationJob::class.java)
                logger.info { "Received notification job for probe ${job.probeId}" }

                withContext(Dispatchers.IO) {
                    notificationService.sendNotification(
                        job.probeId,
                        job.result,
                        job.previousStatus,
                    )
                }

                message.ack()
                future.complete(null)
            } catch (e: Exception) {
                logger.error(e) { "Failed to send notification for probe" }
                future.completeExceptionally(e)
            }
        }

        return future
    }
}
