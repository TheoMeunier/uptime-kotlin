package tmenier.fr.incidents.services

import tmenier.fr.common.enums.incidents.IncidentState
import tmenier.fr.common.utils.toHumanReadable
import tmenier.fr.incidents.dtos.IncidentExport
import tmenier.fr.incidents.dtos.IncidentExportRow
import java.time.Duration
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object IncidentCsvWriter {
    private const val BYTE_ORDER_MARK = "﻿"
    private const val LINE_END = "\r\n"
    private val FORMULA_TRIGGERS = setOf('=', '+', '-', '@', '\t', '\r')

    private val HEADER =
        listOf(
            "incident_id",
            "probe_id",
            "probe_name",
            "state",
            "started_at",
            "confirmed_at",
            "ended_at",
            "duration_seconds",
            "duration",
            "downtime_in_period_seconds",
            "closed_reason",
            "acknowledged_at",
            "opening_message",
            "note",
        )

    fun write(export: IncidentExport): String {
        val lines = listOf(HEADER.joinToString(",")) + export.rows.map { line(it, export.zone) }

        return lines.joinToString(LINE_END, prefix = BYTE_ORDER_MARK, postfix = LINE_END)
    }

    /** `incidents-2026-09.csv` for a calendar month, `incidents-2026-09-16_2026-10-16.csv` otherwise. */
    fun fileName(export: IncidentExport): String {
        val firstDay = export.from.atZone(export.zone).toLocalDate()
        val month = YearMonth.from(firstDay)
        val next = month.plusMonths(1)

        fun startOf(yearMonth: YearMonth): Instant = yearMonth.atDay(1).atStartOfDay(export.zone).toInstant()

        if (export.from == startOf(month) && export.to == startOf(next)) return "incidents-$month.csv"

        return "incidents-${firstDay}_${export.to.atZone(export.zone).toLocalDate()}.csv"
    }

    private fun line(
        row: IncidentExportRow,
        zone: ZoneId,
    ): String {
        val incident = row.incident
        val state = if (incident.endedAt == null) IncidentState.OPEN else IncidentState.CLOSED

        return listOf(
            incident.id.toString(),
            incident.probeId.toString(),
            text(incident.probeName),
            state.name.lowercase(),
            date(incident.startedAt, zone),
            date(incident.confirmedAt, zone),
            date(incident.endedAt, zone),
            incident.durationSeconds?.toString().orEmpty(),
            incident.durationSeconds?.let { Duration.ofSeconds(it).toHumanReadable() }.orEmpty(),
            row.downtimeInPeriodSeconds.toString(),
            incident.closedReason?.name.orEmpty(),
            date(incident.acknowledgedAt, zone),
            text(incident.openingMessage),
            text(incident.note),
        ).joinToString(",")
    }

    private fun date(
        value: Instant?,
        zone: ZoneId,
    ): String =
        value
            ?.truncatedTo(ChronoUnit.SECONDS)
            ?.atZone(zone)
            ?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .orEmpty()

    private fun text(value: String?): String {
        if (value.isNullOrEmpty()) return ""

        val inert = if (value.first() in FORMULA_TRIGGERS) "'$value" else value
        if (inert.none { it == '"' || it == ',' || it == '\n' || it == '\r' }) return inert

        return "\"${inert.replace("\"", "\"\"")}\""
    }
}
