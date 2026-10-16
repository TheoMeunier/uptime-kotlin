package tmenier.fr.incidents

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import jakarta.validation.Validator
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.incidents.IncidentClosedReason
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus.FAILURE
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus.SUCCESS
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.repositories.IncidentRepository
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.incidents.actions.AcknowledgeIncidentAction
import tmenier.fr.incidents.actions.UpdateIncidentNoteAction
import tmenier.fr.incidents.requests.UpdateIncidentNoteRequest
import tmenier.fr.schedulers.services.SaveProbeMonitor
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class IncidentAcknowledgementTest {
    @Inject
    lateinit var probes: IncidentFixtures

    @Inject
    lateinit var fixtures: AcknowledgementFixtures

    @Inject
    lateinit var saveProbeMonitor: SaveProbeMonitor

    @Inject
    lateinit var acknowledge: AcknowledgeIncidentAction

    @Inject
    lateinit var updateNote: UpdateIncidentNoteAction

    @Inject
    lateinit var validator: Validator

    private val created = mutableListOf<UUID>()
    private val t0 = Instant.now().truncatedTo(ChronoUnit.SECONDS)

    @AfterEach
    fun cleanUp() = created.forEach(probes::delete)

    @Test
    fun `acknowledging an ongoing incident twice keeps the first acknowledgement`() {
        val probe = probes.probe().also(created::add)
        check(probe, FAILURE, at = t0)
        val incident = probes.incidents(probe).single()

        val first = acknowledge.execute(incident.id, userId = null)
        val again = acknowledge.execute(incident.id, userId = null)

        assertNotNull(first.acknowledgedAt)
        assertEquals(first.acknowledgedAt, again.acknowledgedAt, "the first acknowledgement wins")
    }

    @Test
    fun `a resolved incident can no longer be acknowledged, and an unknown one is not found`() {
        val probe = probes.probe().also(created::add)
        check(probe, FAILURE, at = t0)
        check(probe, SUCCESS, at = t0.plusSeconds(60))
        val incident = probes.incidents(probe).single()

        val closed = assertThrows(ApiException::class.java) { acknowledge.execute(incident.id, userId = null) }
        assertEquals(Response.Status.CONFLICT, closed.httpStatus)
        assertEquals(AcknowledgeIncidentAction.INCIDENT_CLOSED, closed.errorCode)
        assertNull(probes.incidents(probe).single().acknowledgedAt)

        assertThrows(NotFoundException::class.java) { acknowledge.execute(UUID.randomUUID(), userId = null) }
    }

    @Test
    fun `an acknowledged outage gets no reminder, its recovery still closes it, and the next outage is reminded again`() {
        val probe = probes.probe().also(created::add)
        fixtures.repeatAlertsEvery(probe, seconds = 60)

        // Announced at t0, so a reminder is due from t0 + 60 s.
        check(probe, FAILURE, at = t0)
        acknowledge.execute(probes.incidents(probe).single().id, userId = null)
        check(probe, FAILURE, at = t0.plusSeconds(120))
        assertEquals(0, fixtures.alertRepeatCount(probe), "overdue, but acknowledged: no reminder")

        check(probe, SUCCESS, at = t0.plusSeconds(180))
        val resolved = probes.incidents(probe).single()
        assertEquals(t0.plusSeconds(180), resolved.endedAt, "the recovery is announced and closes the incident")
        assertNotNull(resolved.acknowledgedAt, "the acknowledgement stays in the history")

        // A new outage starts unacknowledged.
        check(probe, FAILURE, at = t0.plusSeconds(240))
        check(probe, FAILURE, at = t0.plusSeconds(360))
        assertEquals(1, fixtures.alertRepeatCount(probe), "the next outage is reminded again")
    }

    @Test
    fun `a check closing an incident it read before the acknowledgement keeps the acknowledgement`() {
        val probe = probes.probe().also(created::add)
        check(probe, FAILURE, at = t0)

        fixtures.acknowledgeThenCloseStaleCopy(probe, at = t0.plusSeconds(60))

        val incident = probes.incidents(probe).single()
        assertNotNull(incident.endedAt)
        assertNotNull(incident.acknowledgedAt, "the close must not write the acknowledgement back to null")
    }

    @Test
    fun `the note can be written, changed and cleared, even once the incident is resolved`() {
        val probe = probes.probe().also(created::add)
        check(probe, FAILURE, at = t0)
        check(probe, SUCCESS, at = t0.plusSeconds(60))
        val id = probes.incidents(probe).single().id

        val written = updateNote.execute(id, note("  certificate expired  "))
        assertEquals("certificate expired", written.note)
        assertNotNull(written.noteUpdatedAt)

        assertEquals("renewed at 09:40", updateNote.execute(id, note("renewed at 09:40")).note)
        assertNull(updateNote.execute(id, note("   ")).note, "a blank note clears it")

        assertThrows(NotFoundException::class.java) { updateNote.execute(UUID.randomUUID(), note("lost")) }
    }

    @Test
    fun `a note longer than 2000 characters is refused`() {
        assertTrue(validator.validate(note("x".repeat(2_000))).isEmpty())
        assertEquals(1, validator.validate(note("x".repeat(2_001))).size)
    }

    private fun note(text: String) = UpdateIncidentNoteRequest().apply { note = text }

    private fun check(
        probeId: UUID,
        status: ProbeMonitorLogStatus,
        at: Instant,
    ) {
        saveProbeMonitor.saveProbeMonitorLog(
            probeId = probeId,
            runAt = at,
            result = ProbeResult(status = status, responseTime = 1, message = status.name.lowercase(), runAt = at),
        )
    }
}

@ApplicationScoped
class AcknowledgementFixtures(
    private val em: EntityManager,
    private val incidentRepository: IncidentRepository,
) {
    @Transactional
    fun repeatAlertsEvery(
        probeId: UUID,
        seconds: Int,
    ) {
        em.find(ProbesEntity::class.java, probeId).alertRepeatSeconds = seconds
    }

    @Transactional
    fun alertRepeatCount(probeId: UUID): Int = em.find(ProbesEntity::class.java, probeId).alertRepeatCount

    /** A check that read the open incident just before the API acknowledged it, then closes it. */
    @Transactional
    fun acknowledgeThenCloseStaleCopy(
        probeId: UUID,
        at: Instant,
    ) {
        val stale = checkNotNull(incidentRepository.findOpen(probeId))
        incidentRepository.acknowledge(stale.id, by = null, at = at)
        incidentRepository.close(probeId, at, IncidentClosedReason.RECOVERY)
    }
}
