package tmenier.fr.incidents.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.BadRequestException
import tmenier.fr.common.enums.incidents.IncidentState
import tmenier.fr.databases.dtos.IncidentFilter
import tmenier.fr.databases.dtos.IncidentPageDto
import tmenier.fr.databases.dtos.IncidentSummaryDto
import tmenier.fr.databases.repositories.IncidentRepository
import java.time.Duration
import java.time.Instant
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
        val until = parseInstant(to, "to") ?: now
        val since = parseInstant(from, "from") ?: until.minus(DEFAULT_SUMMARY_PERIOD)
        requireOrdered(since, until)

        return incidentRepository.summary(since, until, parseUuid(probeId, "probe_id"), now)
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

    private fun requireOrdered(
        from: Instant?,
        to: Instant?,
    ) {
        if (from != null && to != null && !from.isBefore(to)) throw BadRequestException("from must be before to")
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
        private val DEFAULT_SUMMARY_PERIOD: Duration = Duration.ofDays(30)
    }
}
