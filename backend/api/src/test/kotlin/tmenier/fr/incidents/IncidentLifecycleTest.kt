package tmenier.fr.incidents

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.incidents.IncidentClosedReason
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.IncidentDto
import tmenier.fr.databases.dtos.IncidentFilter
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.repositories.IncidentRepository
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.maintenances.actions.StartAdHocMaintenanceAction
import tmenier.fr.maintenances.requests.StartAdHocMaintenanceRequest
import tmenier.fr.monitors.actions.OnOffProbeMonitorAction
import tmenier.fr.schedulers.services.SaveProbeMonitor
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class IncidentLifecycleTest {
    @Inject
    lateinit var fixtures: IncidentFixtures

    @Inject
    lateinit var saveProbeMonitor: SaveProbeMonitor

    @Inject
    lateinit var onOff: OnOffProbeMonitorAction

    @Inject
    lateinit var startAdHocMaintenance: StartAdHocMaintenanceAction

    @Inject
    lateinit var incidents: IncidentRepository

    private val probes = mutableListOf<UUID>()
    private val t0 = Instant.now().truncatedTo(ChronoUnit.SECONDS)

    @AfterEach
    fun cleanUp() = probes.forEach(fixtures::delete)

    @Test
    fun `an outage opens one incident at the alert and closes it at the recovery`() {
        val probe = fixtures.probe(retry = 1).also(probes::add)

        check(probe, ProbeMonitorLogStatus.WARNING, at = t0, message = "connection refused")
        assertEquals(0, fixtures.incidents(probe).size, "a WARNING is not alerted, so it is not an incident yet")

        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.plusSeconds(60), message = "connection refused")
        val open = fixtures.incidents(probe).single()
        assertEquals(t0, open.startedAt, "the incident starts at the first failed check, not at the alert")
        assertEquals(t0.plusSeconds(60), open.confirmedAt)
        assertNull(open.endedAt)
        assertEquals("connection refused", open.openingMessage)

        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.plusSeconds(120))
        check(probe, ProbeMonitorLogStatus.WARNING, at = t0.plusSeconds(180))
        assertEquals(1, fixtures.incidents(probe).size, "still the same outage")

        check(probe, ProbeMonitorLogStatus.SUCCESS, at = t0.plusSeconds(240))
        val closed = fixtures.incidents(probe).single()
        assertEquals(t0.plusSeconds(240), closed.endedAt)
        assertEquals(240L, closed.durationSeconds)
        assertEquals(IncidentClosedReason.RECOVERY, closed.closedReason)
    }

    @Test
    fun `each outage is its own incident`() {
        val probe = fixtures.probe().also(probes::add)

        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0)
        check(probe, ProbeMonitorLogStatus.SUCCESS, at = t0.plusSeconds(60))
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.plusSeconds(120))

        val list = fixtures.incidents(probe)
        assertEquals(2, list.size)
        assertEquals(1, list.count { it.endedAt == null })
    }

    @Test
    fun `disabling the probe closes its incident, and a new outage after re-enabling opens another`() {
        val probe = fixtures.probe().also(probes::add)
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0)

        onOff.execute(probe, enabled = false)
        val closed = fixtures.incidents(probe).single()
        assertNotNull(closed.endedAt)
        assertEquals(IncidentClosedReason.PROBE_DISABLED, closed.closedReason)

        onOff.execute(probe, enabled = true)
        check(probe, ProbeMonitorLogStatus.FAILURE, at = Instant.now())
        assertEquals(2, fixtures.incidents(probe).size)
    }

    @Test
    fun `switching an enabled probe on again ends its incident, and the outage still going on opens a new one`() {
        val probe = fixtures.probe().also(probes::add)
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0)

        onOff.execute(probe, enabled = true)
        check(probe, ProbeMonitorLogStatus.FAILURE, at = Instant.now())

        val list = fixtures.incidents(probe)
        assertEquals(2, list.size, "the switch ends the first one, the new announcement opens the second")
        assertEquals(1, list.count { it.endedAt == null })
    }

    @Test
    fun `an outage contained in a maintenance window is not an incident`() {
        val probe = fixtures.probe().also(probes::add)
        startAdHocMaintenance.execute(probe, StartAdHocMaintenanceRequest().apply { durationMinutes = 10 })
        val now = Instant.now()

        check(probe, ProbeMonitorLogStatus.FAILURE, at = now.plusSeconds(5))
        check(probe, ProbeMonitorLogStatus.SUCCESS, at = now.plusSeconds(65))

        assertEquals(0, fixtures.incidents(probe).size)
    }

    @Test
    fun `the summary counts ongoing incidents and averages the closed ones`() {
        val probe = fixtures.probe().also(probes::add)
        val from = t0.minus(1, ChronoUnit.DAYS)

        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(3_600))
        check(probe, ProbeMonitorLogStatus.SUCCESS, at = t0.minusSeconds(3_600 - 120))
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(1_800))
        check(probe, ProbeMonitorLogStatus.SUCCESS, at = t0.minusSeconds(1_800 - 600))
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(60))

        val summary = fixtures.summary(from, t0.plusSeconds(1), probe, now = t0)

        assertEquals(3L, summary.count)
        assertEquals(1L, summary.ongoing)
        assertEquals(120L + 600L + 60L, summary.totalDowntimeSeconds)
        assertEquals(360L, summary.mttrSeconds)
        assertEquals(600L, summary.longest?.durationSeconds)
    }

    @Test
    fun `an outage that started before the period is still ongoing in it, for the part inside it`() {
        val probe = fixtures.probe().also(probes::add)
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(3_600))

        val summary = fixtures.summary(from = t0.minusSeconds(1_800), to = t0, probeId = probe, now = t0)

        assertEquals(1L, summary.count)
        assertEquals(1L, summary.ongoing)
        assertEquals(1_800L, summary.totalDowntimeSeconds, "the downtime before the period is left out")
        assertNull(summary.mttrSeconds)
        assertEquals(t0.minusSeconds(3_600), summary.longest?.startedAt)
    }

    @Test
    fun `the summary only counts the part of each outage inside the period`() {
        val probe = fixtures.probe().also(probes::add)
        val from = t0.minusSeconds(3_300)
        val to = t0.minusSeconds(1_800)

        // Over before the period.
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(7_200))
        check(probe, ProbeMonitorLogStatus.SUCCESS, at = t0.minusSeconds(6_600))
        // 600 s, 300 s of them after `from`.
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(3_600))
        check(probe, ProbeMonitorLogStatus.SUCCESS, at = t0.minusSeconds(3_000))
        // 1 200 s, 600 s of them before `to`.
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(2_400))
        check(probe, ProbeMonitorLogStatus.SUCCESS, at = t0.minusSeconds(1_200))
        // Started after the period.
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(600))

        val summary = fixtures.summary(from = from, to = to, probeId = probe, now = t0)

        assertEquals(2L, summary.count)
        assertEquals(0L, summary.ongoing)
        assertEquals(300L + 600L, summary.totalDowntimeSeconds, "never more than the period itself")
        assertEquals(900L, summary.mttrSeconds, "the time to recover is the whole outage")
        assertEquals(1_200L, summary.longest?.durationSeconds)

        val history = fixtures.incidents(probe, from = from, to = to)
        assertEquals(listOf(t0.minusSeconds(2_400), t0.minusSeconds(3_600)), history.map { it.startedAt }, "the list shows the same incidents")
    }

    private fun check(
        probeId: UUID,
        status: ProbeMonitorLogStatus,
        at: Instant,
        message: String = status.name.lowercase(),
    ) {
        saveProbeMonitor.saveProbeMonitorLog(
            probeId = probeId,
            runAt = at,
            result = ProbeResult(status = status, responseTime = 1, message = message, runAt = at),
        )
    }
}

@ApplicationScoped
class IncidentFixtures(
    private val em: EntityManager,
    private val objectMapper: ObjectMapper,
    private val incidentRepository: IncidentRepository,
) {
    @Transactional
    fun probe(retry: Int = 0): UUID {
        val probe =
            ProbesEntity().apply {
                id = UUID.randomUUID()
                name = "it-incident-${id.toString().take(8)}"
                interval = 3_600
                timeout = 5
                this.retry = retry
                intervalRetry = 60
                enabled = true
                status = ProbeMonitorLogStatus.SUCCESS
                protocol = ProbeProtocol.TCP
                content = objectMapper.valueToTree(mapOf("url" to "127.0.0.1", "tcpPort" to 1))
            }
        em.persist(probe)
        return probe.id
    }

    @Transactional
    fun incidents(
        probeId: UUID,
        from: Instant? = null,
        to: Instant? = null,
    ): List<IncidentDto> = incidentRepository.findPage(IncidentFilter(probeId = probeId, from = from, to = to), page = 0, size = 50).items

    @Transactional
    fun summary(
        from: Instant,
        to: Instant,
        probeId: UUID,
        now: Instant,
    ) = incidentRepository.summary(from, to, probeId, now)

    @Transactional
    fun delete(probeId: UUID) {
        em
            .createNativeQuery(
                """
                DELETE FROM maintenance_windows w
                WHERE w.id IN (SELECT maintenance_window_id FROM maintenance_window_probes WHERE probe_id = :id)
                """.trimIndent(),
            ).setParameter("id", probeId)
            .executeUpdate()
        em.createNativeQuery("DELETE FROM probes_monitors_logs WHERE probe_id = :id").setParameter("id", probeId).executeUpdate()
        em.createNativeQuery("DELETE FROM probes WHERE id = :id").setParameter("id", probeId).executeUpdate()
    }
}
