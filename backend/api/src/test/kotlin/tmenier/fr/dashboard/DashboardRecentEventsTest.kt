package tmenier.fr.dashboard

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.incidents.IncidentClosedReason
import tmenier.fr.common.enums.incidents.IncidentEventType.CLOSED
import tmenier.fr.common.enums.incidents.IncidentEventType.OPENED
import tmenier.fr.databases.dtos.IncidentEventDto
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.repositories.DashboardRepository
import tmenier.fr.databases.repositories.IncidentRepository
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.incidents.IncidentFixtures
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class DashboardRecentEventsTest {
    @Inject
    lateinit var probes: IncidentFixtures

    @Inject
    lateinit var fixtures: RecentEventsFixtures

    private val created = mutableListOf<UUID>()

    // In the future, so that these incidents are the newest ones whatever else the database holds.
    private val base = Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS)

    @AfterEach
    fun cleanUp() = created.forEach(probes::delete)

    @Test
    fun `each incident gives its opening and its closing, newest first`() {
        val api = probes.probe().also(created::add)
        val db = probes.probe().also(created::add)

        fixtures.incident(api, startedAt = base, endedAt = base.plusSeconds(600))
        fixtures.incident(db, startedAt = base.plusSeconds(300))
        fixtures.incident(api, startedAt = base.plusSeconds(900), endedAt = base.plusSeconds(960), reason = IncidentClosedReason.PROBE_DISABLED)

        val events = fixtures.recentEvents(limit = 5)

        assertEquals(
            listOf(
                Triple(api, CLOSED, base.plusSeconds(960)),
                Triple(api, OPENED, base.plusSeconds(900)),
                Triple(api, CLOSED, base.plusSeconds(600)),
                Triple(db, OPENED, base.plusSeconds(300)),
                Triple(api, OPENED, base),
            ),
            events.map { Triple(it.probeId, it.type, it.at) },
        )

        val (paused, _, recovered, ongoing, opening) = events
        assertEquals(IncidentClosedReason.PROBE_DISABLED, paused.closedReason)
        assertEquals(60L, paused.durationSeconds)
        assertEquals(IncidentClosedReason.RECOVERY, recovered.closedReason)
        assertEquals(600L, recovered.durationSeconds)
        assertTrue(ongoing.ongoing, "the outage of db is still going on")
        assertFalse(opening.ongoing)
        assertNull(opening.durationSeconds, "an opening has no duration")
        assertEquals("down at $base", opening.message)
        assertEquals(opening.incidentId, recovered.incidentId, "both events point to the same incident")
    }

    @Test
    fun `a long outage that ended last comes first, even though it started before the others`() {
        val api = probes.probe().also(created::add)
        val legacy = probes.probe().also(created::add)

        fixtures.incident(api, startedAt = base, endedAt = base.plusSeconds(60))
        fixtures.incident(api, startedAt = base.plusSeconds(120), endedAt = base.plusSeconds(180))
        fixtures.incident(legacy, startedAt = base.minus(1, ChronoUnit.DAYS), endedAt = base.plusSeconds(600))
        fixtures.disable(legacy)

        val events = fixtures.recentEvents(limit = 3)

        assertEquals(
            listOf(
                Triple(legacy, CLOSED, base.plusSeconds(600)),
                Triple(api, CLOSED, base.plusSeconds(180)),
                Triple(api, OPENED, base.plusSeconds(120)),
            ),
            events.map { Triple(it.probeId, it.type, it.at) },
            "a disabled probe keeps its history on the dashboard",
        )
        assertEquals(86_400L + 600L, events.first().durationSeconds)
    }

    @Test
    fun `an incident closed at the instant it opened lists its closing first`() {
        val api = probes.probe().also(created::add)

        fixtures.incident(api, startedAt = base, endedAt = base)

        assertEquals(listOf(CLOSED, OPENED), fixtures.recentEvents(limit = 2).map { it.type })
    }
}

@ApplicationScoped
class RecentEventsFixtures(
    private val em: EntityManager,
    private val incidentRepository: IncidentRepository,
    private val dashboardRepository: DashboardRepository,
) {
    @Transactional
    fun incident(
        probeId: UUID,
        startedAt: Instant,
        endedAt: Instant? = null,
        reason: IncidentClosedReason = IncidentClosedReason.RECOVERY,
    ) {
        val probe = em.find(ProbesEntity::class.java, probeId)
        incidentRepository.open(probe, startedAt = startedAt, confirmedAt = startedAt, message = "down at $startedAt", checkTaskId = null)
        endedAt?.let { incidentRepository.close(probeId, it, reason) }
    }

    @Transactional
    fun disable(probeId: UUID) {
        em.find(ProbesEntity::class.java, probeId).enabled = false
    }

    @Transactional
    fun recentEvents(limit: Int): List<IncidentEventDto> = dashboardRepository.getRecentEvents(limit)
}
