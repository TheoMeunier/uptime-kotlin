package tmenier.fr.databases.repositories

import java.time.Duration

object NotificationRetryDelay {
    private val DELAYS =
        listOf(
            Duration.ofSeconds(2),
            Duration.ofSeconds(10),
            Duration.ofSeconds(30),
            Duration.ofMinutes(2),
        )

    val RATE_LIMIT_PATIENCE: Duration = Duration.ofMinutes(15)

    fun after(failedAttempts: Int): Duration = DELAYS[(failedAttempts - 1).coerceIn(0, DELAYS.lastIndex)]
}
