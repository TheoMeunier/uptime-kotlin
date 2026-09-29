package tmenier.fr.schedulers.templates

import java.time.Duration


object ProbeTimeout {
    const val DEFAULT_SECONDS = 5
    const val MIN_SECONDS = 1
    const val MAX_SECONDS = 120

    fun seconds(configured: Int): Int = if (configured <= 0) DEFAULT_SECONDS else configured.coerceIn(MIN_SECONDS, MAX_SECONDS)

    fun duration(configured: Int): Duration = Duration.ofSeconds(seconds(configured).toLong())
}
