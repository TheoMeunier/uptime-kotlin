package tmenier.fr.common.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ExecutorShutdownTest {
    @Test
    fun `lets a running task finish before the timeout`() {
        val executor = Executors.newSingleThreadExecutor()
        val started = CountDownLatch(1)
        val completed = AtomicBoolean(false)
        executor.submit {
            started.countDown()
            Thread.sleep(200)
            completed.set(true)
        }
        started.await(1, TimeUnit.SECONDS)

        val graceful = executor.shutdownGracefully("test", Duration.ofSeconds(5))

        assertTrue(graceful)
        assertTrue(completed.get())
        assertTrue(executor.isTerminated)
    }

    @Test
    fun `interrupts a task that outlives the timeout`() {
        val executor = Executors.newSingleThreadExecutor()
        val started = CountDownLatch(1)
        val interrupted = CountDownLatch(1)
        executor.submit {
            started.countDown()
            try {
                Thread.sleep(10_000)
            } catch (_: InterruptedException) {
                interrupted.countDown()
            }
        }
        started.await(1, TimeUnit.SECONDS)

        val graceful = executor.shutdownGracefully("test", Duration.ofMillis(100))

        assertFalse(graceful)
        assertTrue(interrupted.await(1, TimeUnit.SECONDS))
        assertTrue(executor.isShutdown)
    }
}
