package tmenier.fr.incidents

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.incidents.IncidentClosedReason
import tmenier.fr.databases.dtos.IncidentPeriodDto
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.repositories.IncidentRepository
import tmenier.fr.deployments.ApiOnlyProfile
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class IncidentPeriodsTest {
    @Inject
    lateinit var probes: IncidentFixtures

    @Inject
    lateinit var fixtures: IncidentPeriodsFixtures

    private val created = mutableListOf<UUID>()
    private val t0 = Instant.now().truncatedTo(ChronoUnit.SECONDS)

    @AfterEach
    fun cleanUp() = created.forEach(probes::delete)

    @Test
    fun `the chart gets the incidents overlapping its window, oldest first, the ongoing one without an end`() {
        val probe = probes.probe().also(created::add)
        val other = probes.probe().also(created::add)

        fixtures.incident(probe, startedAt = t0.minusSeconds(7_200), endedAt = t0.minusSeconds(5_400))
        fixtures.incident(probe, startedAt = t0.minusSeconds(4_000), endedAt = t0.minusSeconds(3_000))
        fixtures.incident(probe, startedAt = t0.minusSeconds(1_800), endedAt = t0.minusSeconds(1_200))
        fixtures.incident(probe, startedAt = t0.minusSeconds(600))
        fixtures.incident(other, startedAt = t0.minusSeconds(900), endedAt = t0.minusSeconds(300))

        val periods = fixtures.periods(probe, from = t0.minusSeconds(3_600), to = t0)

        assertEquals(
            listOf(t0.minusSeconds(4_000), t0.minusSeconds(1_800), t0.minusSeconds(600)),
            periods.map { it.startedAt },
            "the one over before the window and the other probe are left out, the one across its start is kept",
        )
        assertEquals(t0.minusSeconds(3_000), periods.first().endedAt)
        assertNull(periods.last().endedAt, "the ongoing incident has no end yet")
    }

    @Test
    fun `beyond the limit, the oldest incidents are left out`() {
        val probe = probes.probe().also(created::add)

        (5 downTo 1).forEach { n ->
            fixtures.incident(probe, startedAt = t0.minusSeconds(n * 600L), endedAt = t0.minusSeconds(n * 600L - 60))
        }

        val periods = fixtures.periods(probe, from = t0.minusSeconds(3_600), to = t0, limit = 3)

        assertEquals(listOf(t0.minusSeconds(1_800), t0.minusSeconds(1_200), t0.minusSeconds(600)), periods.map { it.startedAt })
    }
}

@ApplicationScoped
class IncidentPeriodsFixtures(
    private val em: EntityManager,
    private val incidentRepository: IncidentRepository,
) {
    @Transactional
    fun incident(
        probeId: UUID,
        startedAt: Instant,
        endedAt: Instant? = null,
    ) {
        val probe = em.find(ProbesEntity::class.java, probeId)
        incidentRepository.open(probe, startedAt = startedAt, confirmedAt = startedAt, message = "down", checkTaskId = null)
        endedAt?.let { incidentRepository.close(probeId, it, IncidentClosedReason.RECOVERY) }
    }

    @Transactional
    fun periods(
        probeId: UUID,
        from: Instant,
        to: Instant,
        limit: Int = IncidentRepository.PERIODS_LIMIT,
    ): List<IncidentPeriodDto> = incidentRepository.findPeriods(probeId, from, to, limit)
}
