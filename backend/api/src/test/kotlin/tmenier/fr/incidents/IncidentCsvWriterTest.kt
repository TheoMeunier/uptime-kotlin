package tmenier.fr.incidents

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.incidents.IncidentClosedReason
import tmenier.fr.databases.dtos.IncidentDto
import tmenier.fr.incidents.dtos.IncidentExport
import tmenier.fr.incidents.dtos.IncidentExportRow
import tmenier.fr.incidents.services.IncidentCsvWriter
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

class IncidentCsvWriterTest {
    private val paris = ZoneId.of("Europe/Paris")
    private val september = Instant.parse("2026-08-31T22:00:00Z") to Instant.parse("2026-09-30T22:00:00Z")

    @Test
    fun `the file starts with a byte order mark and the header, and ends each line with CRLF`() {
        val csv = IncidentCsvWriter.write(export(rows = emptyList()))

        assertTrue(csv.startsWith("﻿incident_id,probe_id,probe_name,state,"), csv)
        assertTrue(csv.endsWith("note\r\n"), csv)
    }

    @Test
    fun `a resolved incident is written with its dates in the reader's time zone`() {
        val incident =
            incident(
                startedAt = Instant.parse("2026-09-03T14:02:11.483Z"),
                confirmedAt = Instant.parse("2026-09-03T14:03:11Z"),
                endedAt = Instant.parse("2026-09-03T16:07:11Z"),
                durationSeconds = 7_500,
                closedReason = IncidentClosedReason.RECOVERY,
                openingMessage = "HTTP 503",
            )

        val line = lines(export(rows = listOf(IncidentExportRow(incident, 7_500)))).single()

        assertEquals(
            listOf(
                incident.id.toString(),
                incident.probeId.toString(),
                "api",
                "closed",
                "2026-09-03T16:02:11+02:00",
                "2026-09-03T16:03:11+02:00",
                "2026-09-03T18:07:11+02:00",
                "7500",
                "2h 5m",
                "7500",
                "RECOVERY",
                "",
                "HTTP 503",
                "",
            ).joinToString(","),
            line,
        )
    }

    @Test
    fun `an ongoing incident leaves its end, duration and reason empty`() {
        val incident = incident(startedAt = Instant.parse("2026-09-30T20:00:00Z"))

        val cells = lines(export(rows = listOf(IncidentExportRow(incident, 7_200)))).single().split(",")

        assertEquals("open", cells[3])
        assertEquals(listOf("", "", "", "7200", ""), cells.subList(6, 11))
    }

    @Test
    fun `UTC dates end with Z`() {
        val incident = incident(startedAt = Instant.parse("2026-09-03T14:02:11Z"))

        val cells = lines(export(rows = listOf(IncidentExportRow(incident, 0)), zone = ZoneOffset.UTC)).single().split(",")

        assertEquals("2026-09-03T14:02:11Z", cells[4])
    }

    @Test
    fun `free text is quoted when needed and cannot start a formula`() {
        val incident =
            incident(
                probeName = "API, \"prod\"",
                openingMessage = "=HYPERLINK(\"http://example.com\")",
                note = "Certificate renewed\nby Jane",
            )

        val line = lines(export(rows = listOf(IncidentExportRow(incident, 0)))).single()

        assertTrue(line.contains(",\"API, \"\"prod\"\"\","), line)
        assertTrue(line.contains(",\"'=HYPERLINK(\"\"http://example.com\"\")\","), line)
        assertTrue(line.endsWith(",\"Certificate renewed\nby Jane\""), line)
    }

    @Test
    fun `a calendar month gives a monthly file name, even across a change of time`() {
        assertEquals("incidents-2026-09.csv", IncidentCsvWriter.fileName(export(rows = emptyList())))

        val october = export(from = Instant.parse("2026-09-30T22:00:00Z"), to = Instant.parse("2026-10-31T23:00:00Z"), rows = emptyList())
        assertEquals("incidents-2026-10.csv", IncidentCsvWriter.fileName(october))

        val utc = export(from = Instant.parse("2026-09-01T00:00:00Z"), to = Instant.parse("2026-10-01T00:00:00Z"), zone = ZoneOffset.UTC, rows = emptyList())
        assertEquals("incidents-2026-09.csv", IncidentCsvWriter.fileName(utc))
    }

    @Test
    fun `any other period is named after its first and last days`() {
        val rolling = export(from = Instant.parse("2026-09-16T08:30:00Z"), to = Instant.parse("2026-10-16T08:30:00Z"), rows = emptyList())

        assertEquals("incidents-2026-09-16_2026-10-16.csv", IncidentCsvWriter.fileName(rolling))
    }

    private fun lines(export: IncidentExport): List<String> =
        IncidentCsvWriter
            .write(export)
            .removeSuffix("\r\n")
            .split("\r\n")
            .drop(1)

    private fun export(
        from: Instant = september.first,
        to: Instant = september.second,
        zone: ZoneId = paris,
        rows: List<IncidentExportRow>,
    ) = IncidentExport(from = from, to = to, zone = zone, generatedAt = to, rows = rows)

    private fun incident(
        startedAt: Instant = Instant.parse("2026-09-03T14:02:11Z"),
        confirmedAt: Instant = startedAt,
        endedAt: Instant? = null,
        durationSeconds: Long? = null,
        closedReason: IncidentClosedReason? = null,
        probeName: String = "api",
        openingMessage: String = "connection refused",
        note: String? = null,
    ) = IncidentDto(
        id = UUID.randomUUID(),
        probeId = UUID.randomUUID(),
        probeName = probeName,
        startedAt = startedAt,
        confirmedAt = confirmedAt,
        endedAt = endedAt,
        durationSeconds = durationSeconds,
        openingMessage = openingMessage,
        closedReason = closedReason,
        note = note,
    )
}
