package tmenier.fr.databases.repositories

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class NotificationRetryDelayTest {
    @Test
    fun `the first retries come within seconds`() {
        assertEquals(Duration.ofSeconds(2), NotificationRetryDelay.after(1))
        assertEquals(Duration.ofSeconds(10), NotificationRetryDelay.after(2))
        assertEquals(Duration.ofSeconds(30), NotificationRetryDelay.after(3))
    }

    @Test
    fun `later retries wait at most two minutes`() {
        assertEquals(Duration.ofMinutes(2), NotificationRetryDelay.after(4))
        assertEquals(Duration.ofMinutes(2), NotificationRetryDelay.after(10))
    }

    @Test
    fun `an unexpected attempt count falls back to the first delay`() {
        assertEquals(Duration.ofSeconds(2), NotificationRetryDelay.after(0))
    }
}
