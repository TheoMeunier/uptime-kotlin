package tmenier.fr.common.utils

import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

fun ExecutorService.shutdownGracefully(
    name: String,
    timeout: Duration,
): Boolean {
    shutdown()
    try {
        if (awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) return true
        logger.warn { "$name did not finish within ${timeout.seconds}s, interrupting remaining task(s)" }
    } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
        logger.warn { "$name shutdown interrupted, interrupting remaining task(s)" }
    }
    shutdownNow()
    return false
}
