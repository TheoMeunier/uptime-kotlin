package tmenier.fr.statuspages.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.dtos.PublicStatusPageDto
import tmenier.fr.monitors.services.StatusSnapshotService.Companion.CACHE_MAX_AGE_SECONDS
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class PublicStatusPageCache {
    private val entries = ConcurrentHashMap<String, Entry>()

    fun getOrCompute(
        slug: String,
        tag: String,
        now: Instant,
        compute: () -> PublicStatusPageDto,
    ): PublicStatusPageDto {
        entries[slug]
            ?.takeIf { it.tag == tag && it.expiresAt.isAfter(now) }
            ?.let { return it.page }

        entries.values.removeIf { !it.expiresAt.isAfter(now) }

        val page = compute()
        entries[slug] = Entry(tag, page, now.plusSeconds(CACHE_MAX_AGE_SECONDS))
        return page
    }

    private data class Entry(
        val tag: String,
        val page: PublicStatusPageDto,
        val expiresAt: Instant,
    )
}
