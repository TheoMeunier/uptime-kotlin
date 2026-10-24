package tmenier.fr.databases.support

import java.time.Instant
import java.time.temporal.ChronoUnit

data class RollupSplit(
    val from: Instant,
    val to: Instant,
    val rollupStart: Instant,
    val rollupEnd: Instant,
) {
    val usesRollups: Boolean get() = rollupEnd.isAfter(rollupStart)

    companion object {
        fun of(
            from: Instant,
            to: Instant,
            watermark: Instant?,
        ): RollupSplit {
            val firstWholeHour = ceilHour(from)
            val end =
                watermark
                    ?.coerceAtMost(to.truncatedTo(ChronoUnit.HOURS))
                    ?.coerceAtLeast(firstWholeHour)
                    ?: firstWholeHour

            return RollupSplit(from = from, to = to, rollupStart = firstWholeHour, rollupEnd = end)
        }

        private fun ceilHour(instant: Instant): Instant {
            val floor = instant.truncatedTo(ChronoUnit.HOURS)
            return if (floor == instant) floor else floor.plus(1, ChronoUnit.HOURS)
        }
    }
}
