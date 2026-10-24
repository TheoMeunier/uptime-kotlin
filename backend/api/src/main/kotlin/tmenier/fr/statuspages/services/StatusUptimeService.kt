package tmenier.fr.statuspages.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.statuspages.StatusPageUptimePeriod
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.ProbeUptimeBarDto
import tmenier.fr.databases.dtos.PublicStatusUptimeDto
import tmenier.fr.databases.dtos.UptimeBucketDto
import tmenier.fr.databases.repositories.ProbeLogRollupRepository
import tmenier.fr.databases.repositories.StatusPageRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class StatusUptimeService(
    private val statusPageRepository: StatusPageRepository,
    private val rollupRepository: ProbeLogRollupRepository,
) {
    private val cache = ConcurrentHashMap<String, Entry>()

    @Transactional
    fun forPage(
        slug: String,
        zone: ZoneId,
        now: Instant = Instant.now(),
    ): PublicStatusUptimeDto {
        val page = statusPageRepository.findBySlug(slug) ?: throw NotFoundException("Status page not found: $slug")
        val period = page.uptimePeriod
        if (!period.aggregated) return PublicStatusUptimeDto(period, zone.id, emptyList())

        val key = "$slug|${zone.id}|${page.updatedAt}"
        cache[key]?.takeIf { it.expiresAt.isAfter(now) }?.let { return it.value }

        val probeIds =
            page.groups
                .flatMap { it.orderedProbes() }
                .map { it.id }
                .distinct()
        val value = PublicStatusUptimeDto(period, zone.id, bars(probeIds, period, zone, now))

        cache.entries.removeIf { !it.value.expiresAt.isAfter(now) }
        cache[key] = Entry(value, now.plus(CACHE_TTL))
        return value
    }

    fun bars(
        probeIds: List<UUID>,
        period: StatusPageUptimePeriod,
        zone: ZoneId,
        now: Instant,
    ): List<ProbeUptimeBarDto> {
        val starts = bucketStarts(period, LocalDateTime.ofInstant(now, zone))
        val from = starts.first().atZone(zone).toInstant()
        val rows =
            rollupRepository
                .uptimeBuckets(probeIds, from, now, zone, period.bucket)
                .associateBy { it.probeId to it.localStart }

        return probeIds.map { probeId ->
            val buckets =
                starts.map { start ->
                    val row = rows[probeId to start]
                    UptimeBucketDto(
                        startsAt = start.atZone(zone).toInstant(),
                        endsAt = start.plus(period.bucket).atZone(zone).toInstant(),
                        total = row?.total ?: 0,
                        successes = row?.successes ?: 0,
                        failures = row?.failures ?: 0,
                        warnings = row?.warnings ?: 0,
                        maintenance = row?.maintenance ?: 0,
                    )
                }

            val total = buckets.sumOf { it.total }
            ProbeUptimeBarDto(
                probeId = probeId,
                uptimePercent = if (total == 0L) null else buckets.sumOf { it.successes } * 100.0 / total,
                buckets = buckets,
            )
        }
    }

    companion object {
        private val ORIGIN: LocalDateTime = LocalDateTime.of(2000, 1, 1, 0, 0)

        private val CACHE_TTL: Duration = Duration.ofSeconds(60)

        fun bucketStarts(
            period: StatusPageUptimePeriod,
            localNow: LocalDateTime,
        ): List<LocalDateTime> {
            val size = period.bucket.seconds
            val elapsed = Duration.between(ORIGIN, localNow).seconds
            val current = ORIGIN.plusSeconds(Math.floorDiv(elapsed, size) * size)

            return (period.bucketCount - 1 downTo 0).map { current.minusSeconds(it * size) }
        }
    }

    private data class Entry(
        val value: PublicStatusUptimeDto,
        val expiresAt: Instant,
    )
}
