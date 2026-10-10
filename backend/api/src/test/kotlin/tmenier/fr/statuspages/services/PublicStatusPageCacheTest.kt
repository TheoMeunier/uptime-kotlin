package tmenier.fr.statuspages.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.statuspages.StatusPageLayout
import tmenier.fr.databases.dtos.PublicStatusPageDto
import java.time.Instant

class PublicStatusPageCacheTest {
    private val cache = PublicStatusPageCache()
    private val now = Instant.parse("2026-10-09T10:00:00Z")

    private fun page(title: String) = PublicStatusPageDto("prod", title, null, StatusPageLayout.GRID, emptyList())

    @Test
    fun `reuses the page for the same slug and tag within 30 seconds`() {
        val first = cache.getOrCompute("prod", "t1", now) { page("A") }
        val second = cache.getOrCompute("prod", "t1", now.plusSeconds(29)) { page("B") }

        assertSame(first, second)
    }

    @Test
    fun `recomputes once the 30 seconds are over`() {
        cache.getOrCompute("prod", "t1", now) { page("A") }
        val later = cache.getOrCompute("prod", "t1", now.plusSeconds(30)) { page("B") }

        assertEquals("B", later.title)
    }

    @Test
    fun `recomputes when the etag changes`() {
        cache.getOrCompute("prod", "t1", now) { page("A") }
        val changed = cache.getOrCompute("prod", "t2", now.plusSeconds(1)) { page("B") }

        assertEquals("B", changed.title)
    }

    @Test
    fun `keeps one entry per slug`() {
        cache.getOrCompute("prod", "t1", now) { page("A") }
        cache.getOrCompute("staging", "t1", now) { page("S") }
        val prod = cache.getOrCompute("prod", "t1", now.plusSeconds(5)) { page("B") }

        assertEquals("A", prod.title)
    }
}
