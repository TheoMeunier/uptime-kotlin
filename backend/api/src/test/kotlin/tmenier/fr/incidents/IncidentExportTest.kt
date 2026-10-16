package tmenier.fr.incidents

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.auth.services.JwtService
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.deployments.ApiOnlyProfile
import tmenier.fr.incidents.actions.ListIncidentsAction
import tmenier.fr.schedulers.services.SaveProbeMonitor
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class IncidentExportTest {
    @Inject
    lateinit var fixtures: IncidentFixtures

    @Inject
    lateinit var saveProbeMonitor: SaveProbeMonitor

    @Inject
    lateinit var action: ListIncidentsAction

    @Inject
    lateinit var jwtService: JwtService

    private val probes = mutableListOf<UUID>()
    private val t0 = Instant.now().truncatedTo(ChronoUnit.SECONDS)

    @AfterEach
    fun cleanUp() = probes.forEach(fixtures::delete)

    @Test
    fun `the export lists every incident of the period, ongoing ones included, and adds up to the summary`() {
        val probe = fixtures.probe().also(probes::add)
        val from = t0.minusSeconds(3_300)
        val to = t0.minusSeconds(600)

        // Over before the period.
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(7_200))
        check(probe, ProbeMonitorLogStatus.SUCCESS, at = t0.minusSeconds(6_600))
        // 600 s, 300 s of them after `from`.
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(3_600))
        check(probe, ProbeMonitorLogStatus.SUCCESS, at = t0.minusSeconds(3_000))
        // Still going on: 1 200 s of it before `to`.
        check(probe, ProbeMonitorLogStatus.FAILURE, at = t0.minusSeconds(1_800))

        val export = action.export(probe.toString(), from.toString(), to.toString(), "Europe/Paris", now = t0)
        val summary = action.summary(probe.toString(), from.toString(), to.toString(), now = t0)

        assertEquals(listOf(t0.minusSeconds(3_600), t0.minusSeconds(1_800)), export.rows.map { it.incident.startedAt }, "oldest first")
        assertEquals(listOf(300L, 1_200L), export.rows.map { it.downtimeInPeriodSeconds }, "only the part inside the period")
        val ongoing = export.rows.last().incident
        assertNull(ongoing.endedAt, "the ongoing outage is in the report")
        assertEquals(summary.count, export.rows.size.toLong())
        assertEquals(summary.totalDowntimeSeconds, export.rows.sumOf { it.downtimeInPeriodSeconds })
    }

    @Test
    fun `without a period the export covers the last 30 days, in UTC`() {
        val export = action.export(probeId = null, from = null, to = null, timeZone = null, now = t0)

        assertEquals(t0, export.to)
        assertEquals(t0.minus(Duration.ofDays(30)), export.from)
        assertEquals(ZoneOffset.UTC, export.zone)
    }

    @Test
    fun `an unknown time zone is refused`() {
        assertThrows(BadRequestException::class.java) {
            action.export(probeId = null, from = null, to = null, timeZone = "Mars/Olympus")
        }
    }

    @Test
    fun `the endpoint sends a CSV attachment named after the month`() {
        val probe = fixtures.probe().also(probes::add)
        check(probe, ProbeMonitorLogStatus.FAILURE, at = Instant.parse("2026-09-03T14:02:11Z"))
        check(probe, ProbeMonitorLogStatus.SUCCESS, at = Instant.parse("2026-09-03T16:07:11Z"))

        val response =
            given()
                .auth()
                .oauth2(jwtService.generateJwt(UUID.randomUUID(), "Jane Doe", "jane@example.com"))
                .queryParam("probe_id", probe.toString())
                .queryParam("from", "2026-08-31T22:00:00Z")
                .queryParam("to", "2026-09-30T22:00:00Z")
                .queryParam("tz", "Europe/Paris")
                .get("/api/incidents/export")
                .then()
                .statusCode(200)
                .extract()
                .response()

        assertTrue(response.contentType.startsWith("text/csv"), response.contentType)
        assertEquals("attachment; filename=\"incidents-2026-09.csv\"", response.header("Content-Disposition"))

        val body = response.asByteArray()
        assertEquals(listOf(0xEF, 0xBB, 0xBF), body.take(3).map { it.toInt() and 0xFF }, "UTF-8 byte order mark")

        val lines = String(body, Charsets.UTF_8).removePrefix("﻿").trimEnd().split("\r\n")
        assertEquals(2, lines.size, lines.joinToString("\n"))
        assertTrue(lines[1].contains(",2026-09-03T16:02:11+02:00,"), lines[1])
    }

    @Test
    fun `the endpoint requires a signed-in user`() {
        given()
            .get("/api/incidents/export")
            .then()
            .statusCode(401)
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
