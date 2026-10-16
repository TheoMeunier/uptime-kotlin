package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.Tuple
import jakarta.persistence.TypedQuery
import tmenier.fr.common.enums.incidents.IncidentClosedReason
import tmenier.fr.common.enums.incidents.IncidentState
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.IncidentDto
import tmenier.fr.databases.dtos.IncidentFilter
import tmenier.fr.databases.dtos.IncidentPageDto
import tmenier.fr.databases.dtos.IncidentPeriodDto
import tmenier.fr.databases.dtos.IncidentSummaryDto
import tmenier.fr.databases.entities.IncidentEntity
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.IncidentMapper
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class IncidentRepository(
    private val em: EntityManager,
) : PanacheRepositoryBase<IncidentEntity, UUID> {
    fun findOpen(probeId: UUID): IncidentEntity? =
        em
            .createQuery(
                "SELECT i FROM IncidentEntity i WHERE i.probe.id = :probeId AND i.endedAt IS NULL",
                IncidentEntity::class.java,
            ).setParameter("probeId", probeId)
            .resultList
            .firstOrNull()

    fun open(
        probe: ProbesEntity,
        startedAt: Instant,
        confirmedAt: Instant,
        message: String,
        checkTaskId: UUID?,
    ): IncidentEntity {
        findOpen(probe.id)?.let { ongoing ->
            logger.warn { "Incident ${ongoing.id} of probe=${probe.id} is already open, kept as is (check $checkTaskId)" }
            return ongoing
        }

        val incident =
            IncidentEntity().apply {
                id = UUID.randomUUID()
                this.probe = probe
                this.startedAt = minOf(startedAt, confirmedAt)
                this.confirmedAt = confirmedAt
                openingMessage = message
                openingCheckTaskId = checkTaskId
            }
        persist(incident)
        return incident
    }

    fun close(
        probeId: UUID,
        at: Instant,
        reason: IncidentClosedReason,
        checkTaskId: UUID? = null,
    ): IncidentEntity? {
        val incident = findOpen(probeId) ?: return null
        val endedAt = maxOf(at, incident.startedAt)

        incident.endedAt = endedAt
        incident.durationSeconds = Duration.between(incident.startedAt, endedAt).seconds
        incident.closedReason = reason
        incident.closingCheckTaskId = checkTaskId
        return incident
    }

    fun acknowledge(
        id: UUID,
        by: UUID?,
        at: Instant,
    ): Boolean =
        em
            .createQuery(
                """
                UPDATE IncidentEntity i
                SET i.acknowledgedAt = :at, i.acknowledgedBy = :by, i.updatedAt = :at
                WHERE i.id = :id AND i.acknowledgedAt IS NULL AND i.endedAt IS NULL
                """.trimIndent(),
            ).setParameter("at", at)
            .setParameter("by", by)
            .setParameter("id", id)
            .executeUpdate() == 1

    fun updateNote(
        id: UUID,
        note: String?,
        at: Instant,
    ): Boolean =
        em
            .createQuery(
                """
                UPDATE IncidentEntity i
                SET i.note = :note, i.noteUpdatedAt = :at, i.updatedAt = :at
                WHERE i.id = :id
                """.trimIndent(),
            ).setParameter("note", note)
            .setParameter("at", at)
            .setParameter("id", id)
            .executeUpdate() == 1

    fun findPage(
        filter: IncidentFilter,
        page: Int,
        size: Int,
    ): IncidentPageDto {
        val where = whereClause(filter)

        val total =
            em
                .createQuery("SELECT COUNT(i) FROM IncidentEntity i $where", Long::class.javaObjectType)
                .bind(filter)
                .singleResult

        val items =
            em
                .createQuery(
                    "SELECT i FROM IncidentEntity i JOIN FETCH i.probe $where ORDER BY i.startedAt DESC, i.id",
                    IncidentEntity::class.java,
                ).bind(filter)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .resultList
                .map(IncidentMapper::toDto)

        return IncidentPageDto(items = items, total = total, page = page, size = size)
    }

    fun findForExport(
        filter: IncidentFilter,
        limit: Int,
    ): List<IncidentDto> =
        em
            .createQuery(
                "SELECT i FROM IncidentEntity i JOIN FETCH i.probe ${whereClause(filter)} ORDER BY i.startedAt, i.id",
                IncidentEntity::class.java,
            ).bind(filter)
            .setMaxResults(limit)
            .resultList
            .map(IncidentMapper::toDto)

    fun findPeriods(
        probeId: UUID,
        from: Instant,
        to: Instant,
        limit: Int = PERIODS_LIMIT,
    ): List<IncidentPeriodDto> {
        val filter = IncidentFilter(probeId = probeId, from = from, to = to)

        return em
            .createQuery("SELECT i FROM IncidentEntity i ${whereClause(filter)} ORDER BY i.startedAt DESC, i.id", IncidentEntity::class.java)
            .bind(filter)
            .setMaxResults(limit)
            .resultList
            .map(IncidentMapper::toPeriodDto)
            .reversed()
    }

    fun summary(
        from: Instant,
        to: Instant,
        probeId: UUID?,
        now: Instant = Instant.now(),
    ): IncidentSummaryDto {
        val closed = IncidentFilter(state = IncidentState.CLOSED, probeId = probeId, from = from, to = to)
        val open = closed.copy(state = IncidentState.OPEN)

        val closedSpans =
            em
                .createQuery(
                    "SELECT i.startedAt, i.endedAt, i.durationSeconds FROM IncidentEntity i ${whereClause(closed)}",
                    Tuple::class.java,
                ).bind(closed)
                .resultList

        val longestClosed =
            em
                .createQuery(
                    """
                    SELECT i FROM IncidentEntity i JOIN FETCH i.probe ${whereClause(closed)}
                    ORDER BY i.durationSeconds DESC, i.startedAt DESC
                    """.trimIndent(),
                    IncidentEntity::class.java,
                ).bind(closed)
                .setMaxResults(1)
                .resultList
                .firstOrNull()
                ?.let(IncidentMapper::toDto)

        val ongoing =
            em
                .createQuery("SELECT i FROM IncidentEntity i JOIN FETCH i.probe ${whereClause(open)}", IncidentEntity::class.java)
                .bind(open)
                .resultList
                .map { IncidentMapper.toDto(it) to elapsedSeconds(it.startedAt, now) }

        val closedDowntime = closedSpans.sumOf { downtimeWithin(it[0] as Instant, it[1] as Instant, from, to) }
        val ongoingDowntime = ongoing.sumOf { downtimeWithin(it.first.startedAt, now, from, to) }
        val mttr =
            closedSpans
                .map { (it[2] as Number).toLong() }
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.let { Math.round(it) }

        val longestOngoing = ongoing.maxByOrNull { it.second }
        val longest =
            when {
                longestOngoing == null -> longestClosed
                longestClosed == null -> longestOngoing.first
                longestOngoing.second > (longestClosed.durationSeconds ?: 0) -> longestOngoing.first
                else -> longestClosed
            }

        return IncidentSummaryDto(
            from = from,
            to = to,
            count = closedSpans.size.toLong() + ongoing.size,
            ongoing = ongoing.size.toLong(),
            totalDowntimeSeconds = closedDowntime + ongoingDowntime,
            mttrSeconds = mttr,
            longest = longest,
        )
    }

    private fun elapsedSeconds(
        startedAt: Instant,
        now: Instant,
    ): Long = Duration.between(startedAt, now).seconds.coerceAtLeast(0)

    private fun whereClause(filter: IncidentFilter): String {
        val conditions =
            listOfNotNull(
                when (filter.state) {
                    IncidentState.OPEN -> "i.endedAt IS NULL"
                    IncidentState.CLOSED -> "i.endedAt IS NOT NULL"
                    null -> null
                },
                filter.probeId?.let { "i.probe.id = :probeId" },
                filter.from?.let { "(i.endedAt IS NULL OR i.endedAt > :from)" },
                filter.to?.let { "i.startedAt < :to" },
            )
        return if (conditions.isEmpty()) "" else conditions.joinToString(" AND ", prefix = "WHERE ")
    }

    private fun <T> TypedQuery<T>.bind(filter: IncidentFilter): TypedQuery<T> {
        filter.probeId?.let { setParameter("probeId", it) }
        filter.from?.let { setParameter("from", it) }
        filter.to?.let { setParameter("to", it) }
        return this
    }

    companion object {
        const val PERIODS_LIMIT = 200

        /** The part of an outage that falls inside [from, to): what the summary adds up as downtime. */
        fun downtimeWithin(
            startedAt: Instant,
            endedAt: Instant,
            from: Instant,
            to: Instant,
        ): Long = Duration.between(maxOf(startedAt, from), minOf(endedAt, to)).seconds.coerceAtLeast(0)
    }
}
