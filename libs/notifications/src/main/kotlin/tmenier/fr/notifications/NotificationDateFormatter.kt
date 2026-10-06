package tmenier.fr.notifications

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@ApplicationScoped
class NotificationDateFormatter(
    @ConfigProperty(name = "notifications.display-timezone", defaultValue = "UTC")
    zoneName: String,
) {
    val zone: ZoneId = resolve(zoneName)

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(zone)

    /** `06/10/2026 14:32:05 UTC`, or `06/10/2026 16:32:05 Europe/Paris (UTC+02:00)`. */
    fun format(instant: Instant): String = "${formatter.format(instant)} ${label(instant)}"

    private fun label(instant: Instant): String {
        if (zone.normalized() == ZoneOffset.UTC) return "UTC"
        val offset = zone.rules.getOffset(instant)
        val utcOffset = if (offset == ZoneOffset.UTC) "UTC" else "UTC${offset.id}"
        return if (zone is ZoneOffset) utcOffset else "${zone.id} ($utcOffset)"
    }

    companion object {
        fun resolve(zoneName: String): ZoneId =
            try {
                ZoneId.of(zoneName.trim())
            } catch (e: DateTimeException) {
                logger.error { "Unknown notifications.display-timezone \"$zoneName\", falling back to UTC" }
                ZoneOffset.UTC
            }
    }
}
