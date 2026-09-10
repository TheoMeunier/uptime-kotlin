package tmenier.fr.schedulers.services

import tmenier.fr.common.enums.maintenances.MaintenanceRecurrence
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

object MaintenanceRecurrencePlanner {
    private const val MAX_SLOTS = 500
    private const val MAX_ITERATIONS = 10_000

    data class Rule(
        val startsAt: Instant,
        val durationSeconds: Int,
        val recurrence: MaintenanceRecurrence,
        val recurrenceUntil: Instant? = null,
        val zone: ZoneId,
    )

    data class Slot(
        val startsAt: Instant,
        val endsAt: Instant,
    )

    fun plan(
        rule: Rule,
        from: Instant,
        until: Instant,
        limit: Int = MAX_SLOTS,
    ): List<Slot> {
        if (rule.durationSeconds <= 0 || limit <= 0) return emptyList()
        if (!until.isAfter(from)) return emptyList()

        val ceiling = minOf(until, rule.recurrenceUntil ?: until)
        val anchor = rule.startsAt.atZone(rule.zone)

        if (rule.recurrence == MaintenanceRecurrence.ONCE) {
            return if (rule.startsAt >= from && rule.startsAt < ceiling) {
                listOf(slotAt(rule.startsAt, rule.durationSeconds))
            } else {
                emptyList()
            }
        }

        val anchorDate = anchor.toLocalDate()
        val localTime = anchor.toLocalTime()

        val slots = mutableListOf<Slot>()
        var index = firstIndexNear(anchorDate, rule, from)
        var iterations = 0

        while (slots.size < limit && iterations < MAX_ITERATIONS) {
            iterations++

            val start = resolve(occurrenceDate(anchorDate, rule.recurrence, index), localTime, rule.zone)
            if (start >= ceiling) break

            if (start >= from) {
                slots.add(slotAt(start, rule.durationSeconds))
            }

            index++
        }

        return slots
    }

    private fun slotAt(
        start: Instant,
        durationSeconds: Int,
    ): Slot = Slot(startsAt = start, endsAt = start.plusSeconds(durationSeconds.toLong()))

    private fun resolve(
        date: LocalDate,
        time: LocalTime,
        zone: ZoneId,
    ): Instant = ZonedDateTime.of(date, time, zone).toInstant()

    private fun occurrenceDate(
        anchorDate: LocalDate,
        recurrence: MaintenanceRecurrence,
        index: Long,
    ): LocalDate =
        when (recurrence) {
            MaintenanceRecurrence.DAILY -> anchorDate.plusDays(index)
            MaintenanceRecurrence.WEEKLY -> anchorDate.plusWeeks(index)
            MaintenanceRecurrence.MONTHLY -> anchorDate.plusMonths(index)
            MaintenanceRecurrence.ONCE -> anchorDate
        }

    private fun firstIndexNear(
        anchorDate: LocalDate,
        rule: Rule,
        from: Instant,
    ): Long {
        val target = from.atZone(rule.zone).toLocalDate()
        if (!target.isAfter(anchorDate)) return 0

        val periods =
            when (rule.recurrence) {
                MaintenanceRecurrence.DAILY -> ChronoUnit.DAYS.between(anchorDate, target)
                MaintenanceRecurrence.WEEKLY -> ChronoUnit.WEEKS.between(anchorDate, target)
                MaintenanceRecurrence.MONTHLY -> ChronoUnit.MONTHS.between(anchorDate, target)
                MaintenanceRecurrence.ONCE -> 0
            }

        return maxOf(0, periods - 1)
    }
}
