package tmenier.fr.notifications

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.utils.logger
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@ApplicationScoped
class NotificationDateFormatter
@Inject
constructor(
    @ConfigProperty(name = "notifications.display-timezone", defaultValue = "UTC")
    zoneName: String,
    @ConfigProperty(name = "notifications.date-format", defaultValue = DEFAULT_PATTERN)
    datePattern: String,
) {
    constructor(zoneName: String) : this(zoneName, DEFAULT_PATTERN)

    val zone: ZoneId = resolve(zoneName)

    private val formatter: DateTimeFormatter = compile(datePattern, zone)

    fun format(instant: Instant): String = formatter.format(instant.atZone(zone))

    companion object {
        const val DEFAULT_PATTERN = "dd/MM/yyyy HH:mm:ss VV"
        val UTC: ZoneId = ZoneId.of("UTC")
        private val DEFAULT_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_PATTERN, Locale.ENGLISH)

        fun resolve(zoneName: String): ZoneId =
            try {
                ZoneId.of(zoneName.trim())
            } catch (e: DateTimeException) {
                logger.error { "Unknown notifications.display-timezone \"$zoneName\", falling back to UTC" }
                UTC
            }

        private fun compile(
            pattern: String,
            zone: ZoneId,
        ): DateTimeFormatter {
            if (pattern.isBlank()) return DEFAULT_FORMATTER
            return try {
                DateTimeFormatter.ofPattern(pattern.trim(), Locale.ENGLISH).also { it.format(Instant.EPOCH.atZone(zone)) }
            } catch (e: IllegalArgumentException) {
                invalid(pattern)
            } catch (e: DateTimeException) {
                invalid(pattern)
            }
        }

        private fun invalid(pattern: String): DateTimeFormatter {
            logger.error { "Invalid notifications.date-format \"$pattern\", falling back to \"$DEFAULT_PATTERN\"" }
            return DEFAULT_FORMATTER
        }
    }
}
