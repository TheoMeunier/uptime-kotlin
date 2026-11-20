package tmenier.fr.notifications

import java.time.Duration

class NotificationRateLimitedException(
    message: String,
    val retryAfter: Duration,
) : RuntimeException(message)
