package tmenier.fr.databases.support

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class RollupSplitTest {
    private val from = Instant.parse("2026-09-24T14:23:17Z")
    private val to = Instant.parse("2026-10-24T14:23:17Z")

    @Test
    fun `whole hours before the watermark come from the rollups, the rest from the raw table`() {
        val split = RollupSplit.of(from, to, watermark = Instant.parse("2026-10-24T14:00:00Z"))

        assertEquals(Instant.parse("2026-09-24T15:00:00Z"), split.rollupStart, "the partial first hour stays raw")
        assertEquals(Instant.parse("2026-10-24T14:00:00Z"), split.rollupEnd)
        assertTrue(split.usesRollups)
    }

    @Test
    fun `without a watermark everything is read from the raw table`() {
        val split = RollupSplit.of(from, to, watermark = null)

        assertFalse(split.usesRollups)
    }

    @Test
    fun `a watermark older than the window leaves the window to the raw table`() {
        val split = RollupSplit.of(from, to, watermark = Instant.parse("2026-01-01T00:00:00Z"))

        assertFalse(split.usesRollups)
        assertEquals(split.rollupStart, split.rollupEnd)
    }

    @Test
    fun `a watermark past the end of the window stops at its last whole hour`() {
        val split = RollupSplit.of(from, Instant.parse("2026-10-01T10:30:00Z"), watermark = Instant.parse("2026-10-24T14:00:00Z"))

        assertEquals(Instant.parse("2026-10-01T10:00:00Z"), split.rollupEnd)
    }

    @Test
    fun `a window shorter than an hour never uses the rollups`() {
        val split =
            RollupSplit.of(
                Instant.parse("2026-10-24T13:10:00Z"),
                Instant.parse("2026-10-24T13:50:00Z"),
                watermark = Instant.parse("2026-10-24T14:00:00Z"),
            )

        assertFalse(split.usesRollups)
    }

    @Test
    fun `a window starting on a whole hour takes that hour from the rollups`() {
        val split = RollupSplit.of(Instant.parse("2026-10-23T14:00:00Z"), to, watermark = Instant.parse("2026-10-24T14:00:00Z"))

        assertEquals(Instant.parse("2026-10-23T14:00:00Z"), split.rollupStart)
    }
}
