package tmenier.fr.notifications.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.enums.notifications.NotificationChannelsEnum

@ApplicationScoped
class NotificationMetrics(
    private val registry: MeterRegistry,
) {
    fun recordDelivery(
        channel: NotificationChannelsEnum?,
        sent: Boolean,
    ) {
        Counter
            .builder(DELIVERIES)
            .description("Notification delivery attempts made by this instance, by channel and result")
            .tag("channel", channel?.name?.lowercase() ?: CHANNEL_UNKNOWN)
            .tag("result", if (sent) RESULT_SENT else RESULT_FAILED)
            .register(registry)
            .increment()
    }

    companion object {
        const val DELIVERIES = "uptime.notification.deliveries"
        const val RESULT_SENT = "sent"
        const val RESULT_FAILED = "failed"
        const val CHANNEL_UNKNOWN = "unknown"
    }
}
