package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.Tuple
import jakarta.transaction.Transactional
import tmenier.fr.databases.dtos.ProbeMaintenanceDto
import tmenier.fr.databases.dtos.ProbeMaintenanceState
import tmenier.fr.databases.entities.MaintenanceOccurrenceEntity
import tmenier.fr.databases.entities.MaintenanceWindowEntity
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class MaintenanceOccurrenceRepository(
    private val em: EntityManager,
) : PanacheRepositoryBase<MaintenanceOccurrenceEntity, UUID> {
    fun isProbeUnderMaintenance(
        probeId: UUID,
        at: Instant,
    ): Boolean =
        em
            .createQuery(
                """
                SELECT o.id
                FROM MaintenanceOccurrenceEntity o
                JOIN o.window w
                JOIN w.probes p
                WHERE p.id = :probeId
                  AND w.active = true
                  AND o.cancelled = false
                  AND o.startsAt <= :at
                  AND o.endsAt > :at
                """.trimIndent(),
                UUID::class.java,
            ).setParameter("probeId", probeId)
            .setParameter("at", at)
            .setMaxResults(1)
            .resultList
            .isNotEmpty()

    fun findMaintenanceStateByProbe(
        at: Instant,
        horizon: Instant,
        publicOnly: Boolean = false,
    ): Map<UUID, ProbeMaintenanceState> {
        val visibility = if (publicOnly) "AND w.isPublic = true" else ""

        val rows =
            em
                .createQuery(
                    """
                    SELECT p.id, o.id, o.startsAt, o.endsAt, w.title, w.id
                    FROM MaintenanceOccurrenceEntity o
                    JOIN o.window w
                    JOIN w.probes p
                    WHERE w.active = true
                      AND o.cancelled = false
                      AND o.endsAt > :at
                      AND o.startsAt <= :horizon
                      $visibility
                    ORDER BY o.startsAt ASC
                    """.trimIndent(),
                    Tuple::class.java,
                ).setParameter("at", at)
                .setParameter("horizon", horizon)
                .resultList

        return rows
            .groupBy { it[0] as UUID }
            .mapValues { (_, probeRows) ->
                val entries =
                    probeRows.map {
                        ProbeMaintenanceDto(
                            id = it[1] as UUID,
                            windowId = it[5] as UUID,
                            title = it[4] as String,
                            startsAt = it[2] as Instant,
                            endsAt = it[3] as Instant,
                        )
                    }

                ProbeMaintenanceState(
                    current = entries.firstOrNull { it.startsAt <= at },
                    next = entries.firstOrNull { it.startsAt > at },
                )
            }
    }

    fun findByProbeBetween(
        probeId: UUID,
        from: Instant,
        to: Instant,
    ): List<MaintenanceOccurrenceEntity> =
        em
            .createQuery(
                """
                SELECT o
                FROM MaintenanceOccurrenceEntity o
                JOIN o.window w
                JOIN w.probes p
                WHERE p.id = :probeId
                  AND w.active = true
                  AND o.cancelled = false
                  AND o.endsAt > :from
                  AND o.startsAt < :to
                ORDER BY o.startsAt ASC
                """.trimIndent(),
                MaintenanceOccurrenceEntity::class.java,
            ).setParameter("probeId", probeId)
            .setParameter("from", from)
            .setParameter("to", to)
            .resultList

    fun findCurrent(
        windowId: UUID,
        at: Instant,
    ): MaintenanceOccurrenceEntity? =
        find(
            "window.id = ?1 AND cancelled = false AND startsAt <= ?2 AND endsAt > ?3",
            windowId,
            at,
            at,
        ).firstResult()

    fun findNext(
        windowId: UUID,
        at: Instant,
    ): MaintenanceOccurrenceEntity? =
        find(
            "window.id = ?1 AND cancelled = false AND startsAt > ?2 ORDER BY startsAt ASC",
            windowId,
            at,
        ).firstResult()

    fun findUpcoming(
        windowId: UUID,
        from: Instant,
        limit: Int = 10,
    ): List<MaintenanceOccurrenceEntity> =
        find(
            "window.id = ?1 AND endsAt > ?2 ORDER BY startsAt ASC",
            windowId,
            from,
        ).page(0, limit).list()

    fun findCurrentAndNextByWindows(
        windowIds: List<UUID>,
        at: Instant,
    ): Map<UUID, Pair<MaintenanceOccurrenceEntity?, MaintenanceOccurrenceEntity?>> {
        if (windowIds.isEmpty()) return emptyMap()

        return em
            .createQuery(
                """
                SELECT o
                FROM MaintenanceOccurrenceEntity o
                JOIN FETCH o.window w
                WHERE w.id IN :windowIds
                  AND o.cancelled = false
                  AND o.endsAt > :at
                ORDER BY o.startsAt ASC
                """.trimIndent(),
                MaintenanceOccurrenceEntity::class.java,
            ).setParameter("windowIds", windowIds)
            .setParameter("at", at)
            .resultList
            .groupBy { it.window.id }
            .mapValues { (_, occurrences) ->
                occurrences.firstOrNull { it.startsAt <= at } to occurrences.firstOrNull { it.startsAt > at }
            }
    }

    fun sumMaintenanceSecondsByProbe(
        from: Instant,
        to: Instant,
    ): Map<UUID, Long> {
        val rows =
            em
                .createQuery(
                    """
                    SELECT p.id, o.startsAt, o.endsAt
                    FROM MaintenanceOccurrenceEntity o
                    JOIN o.window w
                    JOIN w.probes p
                    WHERE w.active = true
                      AND o.cancelled = false
                      AND o.endsAt > :from
                      AND o.startsAt < :to
                    """.trimIndent(),
                    Tuple::class.java,
                ).setParameter("from", from)
                .setParameter("to", to)
                .resultList

        return rows
            .groupBy { it[0] as UUID }
            .mapValues { (_, probeRows) ->
                val clamped =
                    probeRows.map {
                        maxOf(it[1] as Instant, from) to minOf(it[2] as Instant, to)
                    }

                mergeIntervals(clamped).sumOf { (start, end) -> Duration.between(start, end).seconds }
            }
    }

    private fun mergeIntervals(intervals: List<Pair<Instant, Instant>>): List<Pair<Instant, Instant>> {
        val sorted = intervals.filter { it.second > it.first }.sortedBy { it.first }
        val merged = mutableListOf<Pair<Instant, Instant>>()

        sorted.forEach { (start, end) ->
            val last = merged.lastOrNull()
            if (last != null && start <= last.second) {
                merged[merged.lastIndex] = last.first to maxOf(last.second, end)
            } else {
                merged.add(start to end)
            }
        }

        return merged
    }

    fun findMaterialisedStarts(
        windowId: UUID,
        from: Instant,
        to: Instant,
    ): Set<Instant> =
        find(
            "window.id = ?1 AND startsAt >= ?2 AND startsAt < ?3",
            windowId,
            from,
            to,
        ).list().map { it.startsAt }.toSet()

    fun materialise(
        window: MaintenanceWindowEntity,
        slots: List<Pair<Instant, Instant>>,
    ): Int {
        slots.forEach { (start, end) ->
            val occurrence =
                MaintenanceOccurrenceEntity().apply {
                    id = UUID.randomUUID()
                    this.window = window
                    startsAt = start
                    endsAt = end
                }
            occurrence.persist()
        }

        return slots.size
    }

    fun deleteNotStarted(
        windowId: UUID,
        from: Instant,
    ): Long = delete("window.id = ?1 AND startsAt > ?2", windowId, from)

    @Transactional
    fun cancel(occurrenceId: UUID): Boolean {
        val occurrence = findById(occurrenceId) ?: return false
        if (occurrence.cancelled) return true

        occurrence.cancelled = true
        occurrence.persist()

        return true
    }

    @Transactional
    fun endNow(
        windowId: UUID,
        at: Instant,
    ): Boolean {
        val occurrence = findCurrent(windowId, at) ?: return false

        occurrence.endsAt = maxOf(at, occurrence.startsAt.plusSeconds(1))
        occurrence.persist()

        return true
    }
}
