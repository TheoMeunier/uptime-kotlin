package tmenier.fr.common.utils

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.Duration
import java.time.LocalDateTime

object DowntimeWindow {
    const val LOOKBACK_DAYS = 30L

    fun startOf(now: LocalDateTime): LocalDateTime = now.minusDays(LOOKBACK_DAYS)

    fun resolve(
        lastSuccessAt: LocalDateTime?,
        createdAt: LocalDateTime,
        now: LocalDateTime,
    ): Downtime {
        if (lastSuccessAt != null) return Downtime(since = lastSuccessAt, bounded = false)

        val windowStart = startOf(now)

        return if (createdAt.isAfter(windowStart)) {
            Downtime(since = createdAt, bounded = false)
        } else {
            Downtime(since = windowStart, bounded = true)
        }
    }
}

@RegisterForReflection
data class Downtime(
    val since: LocalDateTime,
    val bounded: Boolean,
) {
    fun humanReadable(now: LocalDateTime): String {
        val duration = Duration.between(since, now).toHumanReadable()

        return if (bounded) "> $duration" else duration
    }
}
