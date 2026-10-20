package tmenier.fr.statuspages

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.statuspages.StatusPageUptimePeriod
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.incidents.IncidentFixtures
import tmenier.fr.statuspages.actions.StoreStatusPageAction
import tmenier.fr.statuspages.requests.StoreStatusPageGroupRequest
import tmenier.fr.statuspages.requests.StoreStatusPageRequest
import tmenier.fr.statuspages.services.StatusUptimeService
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class StatusUptimeTest {
    @Inject
    lateinit var probes: IncidentFixtures

    @Inject
    lateinit var fixtures: StatusPageFixtures

    @Inject
    lateinit var storePage: StoreStatusPageAction

    @Inject
    lateinit var uptime: StatusUptimeService

    private val createdProbes = mutableListOf<UUID>()
    private val createdPages = mutableListOf<UUID>()

    private val paris = ZoneId.of("Europe/Paris")

    /** 14:00 in Paris (UTC+2). */
    private val now = Instant.parse("2026-10-20T12:00:00Z")

    @AfterEach
    fun cleanUp() {
        createdPages.forEach(fixtures::deletePage)
        createdProbes.forEach(probes::delete)
    }

    @Test
    fun `day buckets follow the visitor's midnight and count checks outside maintenance`() {
        val probe = probe()
        val slug = page(StatusPageUptimePeriod.D30, probe)

        fixtures.log(probe, Instant.parse("2026-10-20T08:00:00Z"), SUCCESS)
        fixtures.log(probe, Instant.parse("2026-10-20T09:00:00Z"), SUCCESS)
        fixtures.log(probe, Instant.parse("2026-10-20T10:00:00Z"), FAILURE)
        // 00:30 on the 20th in Paris, still the 19th in UTC.
        fixtures.log(probe, Instant.parse("2026-10-19T22:30:00Z"), SUCCESS)
        // 23:30 on the 19th in Paris.
        fixtures.log(probe, Instant.parse("2026-10-19T21:30:00Z"), WARNING)
        fixtures.log(probe, Instant.parse("2026-10-17T10:00:00Z"), SUCCESS, underMaintenance = true)
        // Before the first bucket (21 September, local).
        fixtures.log(probe, Instant.parse("2026-09-15T10:00:00Z"), SUCCESS)

        val result = uptime.forPage(slug, paris, now)
        assertEquals(StatusPageUptimePeriod.D30, result.period)

        val bar = result.probes.single()
        assertEquals(30, bar.buckets.size)
        assertEquals(Instant.parse("2026-09-20T22:00:00Z"), bar.buckets.first().startsAt, "21 September, local midnight")
        assertEquals(Instant.parse("2026-10-19T22:00:00Z"), bar.buckets.last().startsAt, "20 October, local midnight")

        val today = bar.buckets.last()
        assertEquals(4, today.total)
        assertEquals(3, today.successes)
        assertEquals(1, today.failures)

        val yesterday = bar.buckets[28]
        assertEquals(1, yesterday.total)
        assertEquals(1, yesterday.warnings)

        val maintenanceDay = bar.buckets[26]
        assertEquals(0, maintenanceDay.total)
        assertEquals(1, maintenanceDay.maintenance)

        assertEquals(3 * 100.0 / 5, bar.uptimePercent!!, 0.001, "successes over checks outside maintenance, in the period")
        assertTrue(bar.buckets.take(26).all { it.total == 0L && it.maintenance == 0L }, "no check, no data")
    }

    @Test
    fun `a page showing the last hour gets no aggregated bars`() {
        val slug = page(StatusPageUptimePeriod.H1, probe())

        assertTrue(uptime.forPage(slug, paris, now).probes.isEmpty())
    }

    @Test
    fun `a page cannot go back further than the shortest retention of its monitors`() {
        val short = probe().also { fixtures.retention(it, 30) }
        val forever = probe().also { fixtures.retention(it, 0) }

        assertThrows(BadRequestException::class.java) { page(StatusPageUptimePeriod.D60, short, forever) }
        assertDoesNotThrow { page(StatusPageUptimePeriod.D30, short, forever) }
        assertDoesNotThrow { page(StatusPageUptimePeriod.D90, forever) }
    }

    @Test
    fun `an unknown page has no bars`() {
        assertThrows(NotFoundException::class.java) { uptime.forPage("no-such-page-${UUID.randomUUID()}", paris, now) }
    }

    @Test
    fun `bucket starts are aligned on the local clock, oldest first`() {
        val localNow = LocalDateTime.parse("2026-10-20T14:35:00")

        val hours = StatusUptimeService.bucketStarts(StatusPageUptimePeriod.D1, localNow)
        assertEquals(24, hours.size)
        assertEquals(LocalDateTime.parse("2026-10-19T15:00:00"), hours.first())
        assertEquals(LocalDateTime.parse("2026-10-20T14:00:00"), hours.last())

        val fourHours = StatusUptimeService.bucketStarts(StatusPageUptimePeriod.D7, localNow)
        assertEquals(42, fourHours.size)
        assertEquals(LocalDateTime.parse("2026-10-13T16:00:00"), fourHours.first())
        assertEquals(LocalDateTime.parse("2026-10-20T12:00:00"), fourHours.last())

        val days = StatusUptimeService.bucketStarts(StatusPageUptimePeriod.D90, localNow)
        assertEquals(90, days.size)
        assertEquals(LocalDateTime.parse("2026-10-20T00:00:00"), days.last())
    }

    private fun probe(): UUID = probes.probe().also(createdProbes::add)

    private fun page(
        period: StatusPageUptimePeriod,
        vararg probeIds: UUID,
    ): String {
        val slug = "it-uptime-${UUID.randomUUID().toString().take(8)}"
        val request =
            StoreStatusPageRequest().apply {
                this.slug = slug
                title = slug
                uptimePeriod = period
                groups = listOf(StoreStatusPageGroupRequest().apply { this.probeIds = probeIds.toList() })
            }
        storePage.execute(request).also(createdPages::add)
        return slug
    }

    companion object {
        private val SUCCESS = ProbeMonitorLogStatus.SUCCESS.ordinal
        private val WARNING = ProbeMonitorLogStatus.WARNING.ordinal
        private val FAILURE = ProbeMonitorLogStatus.FAILURE.ordinal
    }
}
