package tmenier.fr.notifications

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class NotificationRetryAfterTest {
    private fun retryAfter(header: String?) = NotificationHttpClient.retryAfter(header)

    @Test
    fun `reads whole and decimal seconds`() {
        assertEquals(Duration.ofSeconds(3), retryAfter("3"))
        assertEquals(Duration.ofMillis(1_500), retryAfter(" 1.5 "))
    }

    @Test
    fun `keeps the wait between one second and five minutes`() {
        assertEquals(Duration.ofSeconds(1), retryAfter("0.2"))
        assertEquals(Duration.ofMinutes(5), retryAfter("3600"))
    }

    @Test
    fun `falls back to five seconds without a usable header`() {
        assertEquals(Duration.ofSeconds(5), retryAfter(null))
        assertEquals(Duration.ofSeconds(5), retryAfter("Wed, 21 Oct 2026 07:28:00 GMT"))
    }
}
