package tmenier.fr.incidents.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.incidents.IncidentState
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.databases.dtos.IncidentFilter
import tmenier.fr.databases.dtos.IncidentPageDto
import tmenier.fr.databases.dtos.IncidentSummaryDto
import tmenier.fr.databases.repositories.IncidentRepository
import tmenier.fr.incidents.dtos.IncidentExport
import tmenier.fr.incidents.dtos.IncidentExportRow
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.UUID

@ApplicationScoped
class ListIncidentsAction(
    private val incidentRepository: IncidentRepository,
) {
    @Transactional
    fun execute(
        state: String?,
        probeId: String?,
        from: String?,
        to: String?,
        page: Int,
        size: Int,
    ): IncidentPageDto {
        if (page < 0) throw BadRequestException("page must be 0 or more")
        if (size !in 1..MAX_PAGE_SIZE) throw BadRequestException("size must be between 1 and $MAX_PAGE_SIZE")

        val filter =
            IncidentFilter(
                state = parseState(state),
                probeId = parseUuid(probeId, "probe_id"),
                from = parseInstant(from, "from"),
                to = parseInstant(to, "to"),
            )
        requireOrdered(filter.from, filter.to)

        return incidentRepository.findPage(filter, page, size)
    }

    @Transactional
    fun summary(
        probeId: String?,
        from: String?,
        to: String?,
        now: Instant = Instant.now(),
    ): IncidentSummaryDto {
        val (since, until) = period(from, to, now)

        return incidentRepository.summary(since, until, parseUuid(probeId, "probe_id"), now)
    }

    /**
     * Every incident of the period, ongoing ones included, with the same period and the same downtime rule
     * as [summary]: the report adds up to the figures shown on the page.
     */
    @Transactional
    fun export(
        probeId: String?,
        from: String?,
        to: String?,
        timeZone: String?,
        now: Instant = Instant.now(),
    ): IncidentExport {
        val (since, until) = period(from, to, now)
        val zone = parseZone(timeZone)
        val filter = IncidentFilter(probeId = parseUuid(probeId, "probe_id"), from = since, to = until)

        val incidents = incidentRepository.findForExport(filter, limit = MAX_EXPORT_ROWS + 1)
        if (incidents.size > MAX_EXPORT_ROWS) {
            throw ApiException(
                EXPORT_TOO_LARGE,
                "More than $MAX_EXPORT_ROWS incidents in this period: export a shorter one.",
                Response.Status.BAD_REQUEST,
            )
        }

        val rows =
            incidents.map {
                IncidentExportRow(it, IncidentRepository.downtimeWithin(it.startedAt, it.endedAt ?: now, since, until))
            }

        return IncidentExport(from = since, to = until, zone = zone, generatedAt = now, rows = rows)
    }

    private fun period(
        from: String?,
        to: String?,
        now: Instant,
    ): Pair<Instant, Instant> {
        val until = parseInstant(to, "to") ?: now
        val since = parseInstant(from, "from") ?: until.minus(DEFAULT_SUMMARY_PERIOD)
        requireOrdered(since, until)

        return since to until
    }

    private fun parseState(value: String?): IncidentState? =
        value?.takeIf { it.isNotBlank() }?.let { raw ->
            IncidentState.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
                ?: throw BadRequestException("state must be one of: open, closed")
        }

    private fun parseUuid(
        value: String?,
        name: String,
    ): UUID? =
        value?.takeIf { it.isNotBlank() }?.let {
            runCatching { UUID.fromString(it) }.getOrElse { throw BadRequestException("$name must be a UUID") }
        }

    private fun parseInstant(
        value: String?,
        name: String,
    ): Instant? =
        value?.takeIf { it.isNotBlank() }?.let {
            try {
                Instant.parse(it)
            } catch (_: DateTimeParseException) {
                throw BadRequestException("$name must be an ISO-8601 instant, e.g. 2026-10-01T00:00:00Z")
            }
        }

    private fun parseZone(value: String?): ZoneId =
        value?.takeIf { it.isNotBlank() }?.let {
            runCatching { ZoneId.of(it) }.getOrElse { throw BadRequestException("tz must be a time zone, e.g. Europe/Paris") }
        } ?: ZoneOffset.UTC

    private fun requireOrdered(
        from: Instant?,
        to: Instant?,
    ) {
        if (from != null && to != null && !from.isBefore(to)) throw BadRequestException("from must be before to")
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
        const val MAX_EXPORT_ROWS = 10_000
        const val EXPORT_TOO_LARGE = "INCIDENT_EXPORT_TOO_LARGE"
        private val DEFAULT_SUMMARY_PERIOD: Duration = Duration.ofDays(30)
    }
}
