package tmenier.fr.common.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class DowntimeWindowTest {
    private val now: LocalDateTime = LocalDateTime.of(2026, 9, 11, 12, 0)

    @Test
    fun `reports the exact downtime when the last success is known`() {
        val downtime =
            DowntimeWindow.resolve(
                lastSuccessAt = now.minusHours(3).minusMinutes(20),
                createdAt = now.minusYears(1),
                now = now,
            )

        assertFalse(downtime.bounded)
        assertEquals("3h 20m", downtime.humanReadable(now))
    }

    @Test
    fun `falls back to the creation date for a probe that never succeeded since it was created`() {
        val createdAt = now.minusDays(2)

        val downtime =
            DowntimeWindow.resolve(
                lastSuccessAt = null,
                createdAt = createdAt,
                now = now,
            )

        assertFalse(downtime.bounded)
        assertEquals(createdAt, downtime.since)
        assertEquals("2d", downtime.humanReadable(now))
    }

    @Test
    fun `never claims a downtime longer than the lookback window`() {
        val downtime =
            DowntimeWindow.resolve(
                lastSuccessAt = null,
                createdAt = now.minusYears(2),
                now = now,
            )

        // Sans borne, l'ancien code répondait « en panne depuis 2 ans » alors que les logs ne
        // disent rien au-delà de la fenêtre : ils ont pu être purgés, ou la sonde a pu réussir.
        assertTrue(downtime.bounded)
        assertEquals(DowntimeWindow.startOf(now), downtime.since)
        assertEquals("> 30d", downtime.humanReadable(now))
    }

    @Test
    fun `treats a success right at the window boundary as exact`() {
        val downtime =
            DowntimeWindow.resolve(
                lastSuccessAt = DowntimeWindow.startOf(now),
                createdAt = now.minusYears(1),
                now = now,
            )

        assertFalse(downtime.bounded)
        assertEquals("30d", downtime.humanReadable(now))
    }
}
