package tmenier.fr.notifications.services

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.notifications.NotificationEvent
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.notifications.NotificationDispatcher
import tmenier.fr.notifications.resolvers.NotificationEventResolver
import java.util.UUID

@ApplicationScoped
class NotificationService(
    val probeRepository: ProbeRepository,
    private val notificationEventResolver: NotificationEventResolver,
    private val notificationDispatcher: NotificationDispatcher
) {
    suspend fun sendNotification(
        probeId: UUID,
        taskId: UUID? = null,
        result: ProbeResult,
        previousStatus: ProbeMonitorLogStatus
    ) {
        val dto = ProbeMapper.toProbeWithNotificationsDto(withContext(Dispatchers.IO) { probeRepository.findById(probeId) })
        val event = notificationEventResolver.resolve(previousStatus, result.status)

        if (event == NotificationEvent.NONE) {
            logger.debug { "Probe ${dto.probe.id}: pas de changement, notifications ignorées" }
            return
        }

        coroutineScope {
            dto.notifications.forEach { notification ->
                launch { notificationDispatcher.dispatch(notification, dto.probe, result, event, taskId) }
            }
        }
    }
}
