package tmenier.fr.schedulers.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.maintenances.MaintenanceRecurrence
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class MaintenanceRecurrencePlannerTest {
    private val paris: ZoneId = ZoneId.of("Europe/Paris")

    private fun utc(text: String): Instant = LocalDateTime.parse(text).toInstant(ZoneOffset.UTC)

    private fun parisWallClock(instant: Instant): String = instant.atZone(paris).toLocalDateTime().toString()

    private fun rule(
        startsAt: Instant,
        recurrence: MaintenanceRecurrence,
        durationSeconds: Int = 7_200,
        until: Instant? = null,
        zone: ZoneId = paris,
    ) = MaintenanceRecurrencePlanner.Rule(
        startsAt = startsAt,
        durationSeconds = durationSeconds,
        recurrence = recurrence,
        recurrenceUntil = until,
        zone = zone,
    )

    @Test
    fun `a one-off window yields its single occurrence, and only inside the range`() {
        val start = utc("2026-09-20T01:00")
        val once = rule(start, MaintenanceRecurrence.ONCE)

        val inside = MaintenanceRecurrencePlanner.plan(once, from = utc("2026-09-19T00:00"), until = utc("2026-09-21T00:00"))
        assertEquals(1, inside.size)
        assertEquals(start, inside.first().startsAt)
        assertEquals(start.plusSeconds(7_200), inside.first().endsAt)

        val before = MaintenanceRecurrencePlanner.plan(once, from = utc("2026-09-21T00:00"), until = utc("2026-09-22T00:00"))
        assertTrue(before.isEmpty())
    }

    @Test
    fun `the range is half-open, so a slot starting exactly at the upper bound is excluded`() {
        val start = utc("2026-09-20T01:00")
        val once = rule(start, MaintenanceRecurrence.ONCE)

        assertEquals(1, MaintenanceRecurrencePlanner.plan(once, from = start, until = start.plusSeconds(1)).size)
        assertTrue(MaintenanceRecurrencePlanner.plan(once, from = start.plusSeconds(1), until = start.plusSeconds(60)).isEmpty())
        assertTrue(MaintenanceRecurrencePlanner.plan(once, from = utc("2026-09-19T00:00"), until = start).isEmpty())
    }

    @Test
    fun `a daily rule keeps its wall clock across the spring-forward transition`() {
        // 02:30 Europe/Paris. On 29 March 2026 that local time does not exist: the clocks jump from
        // 02:00 to 03:00. The window must stay anchored to the clock face, not to the UTC instant.
        val daily = rule(utc("2026-03-20T01:30"), MaintenanceRecurrence.DAILY, durationSeconds = 3_600)

        val slots =
            MaintenanceRecurrencePlanner.plan(
                daily,
                from = utc("2026-03-28T00:00"),
                until = utc("2026-03-31T00:00"),
            )

        assertEquals(3, slots.size)

        // Before the change: 02:30 CET (UTC+1).
        assertEquals(utc("2026-03-28T01:30"), slots[0].startsAt)
        assertEquals("2026-03-28T02:30", parisWallClock(slots[0].startsAt))

        // Inside the gap: pushed past it, to 03:30 CEST — the same instant an offset-naive
        // implementation would produce, but reached deliberately rather than by accident.
        assertEquals(utc("2026-03-29T01:30"), slots[1].startsAt)
        assertEquals("2026-03-29T03:30", parisWallClock(slots[1].startsAt))

        // After the change: 02:30 CEST (UTC+2), an hour earlier in UTC than two days before.
        assertEquals(utc("2026-03-30T00:30"), slots[2].startsAt)
        assertEquals("2026-03-30T02:30", parisWallClock(slots[2].startsAt))
    }

    @Test
    fun `the duration is real time, not clock time, across a transition`() {
        val daily = rule(utc("2026-03-20T01:30"), MaintenanceRecurrence.DAILY, durationSeconds = 7_200)

        val slots = MaintenanceRecurrencePlanner.plan(daily, from = utc("2026-03-28T00:00"), until = utc("2026-03-31T00:00"))

        slots.forEach { slot ->
            assertEquals(Duration.ofHours(2), Duration.between(slot.startsAt, slot.endsAt))
        }
    }

    @Test
    fun `a weekly rule steps seven wall-clock days, not 168 hours`() {
        val weekly = rule(utc("2026-03-22T01:30"), MaintenanceRecurrence.WEEKLY, durationSeconds = 3_600)

        val slots = MaintenanceRecurrencePlanner.plan(weekly, from = utc("2026-03-22T00:00"), until = utc("2026-04-06T00:00"))

        assertEquals(3, slots.size)
        assertEquals(listOf("2026-03-22T02:30", "2026-03-29T03:30", "2026-04-05T02:30"), slots.map { parisWallClock(it.startsAt) })
    }

    @Test
    fun `a monthly rule anchored on the 31st does not drift down to the 28th`() {
        // Stepping month by month would give 31 Jan, 28 Feb, then 28 Mar, 28 Apr… for ever.
        // Computing each occurrence from the anchor gives back the 31st as soon as the month allows.
        val monthly = rule(utc("2026-01-31T01:00"), MaintenanceRecurrence.MONTHLY, durationSeconds = 3_600, zone = ZoneId.of("UTC"))

        val slots = MaintenanceRecurrencePlanner.plan(monthly, from = utc("2026-01-01T00:00"), until = utc("2026-06-01T00:00"))

        assertEquals(
            listOf("2026-01-31T01:00", "2026-02-28T01:00", "2026-03-31T01:00", "2026-04-30T01:00", "2026-05-31T01:00"),
            slots.map { it.atUtc() },
        )
    }

    @Test
    fun `an anchor far in the past does not cost a step per elapsed day`() {
        val daily = rule(utc("2020-01-01T01:00"), MaintenanceRecurrence.DAILY, durationSeconds = 3_600, zone = ZoneId.of("UTC"))

        val slots =
            MaintenanceRecurrencePlanner.plan(
                daily,
                from = utc("2026-09-10T00:00"),
                until = utc("2026-09-13T00:00"),
            )

        assertEquals(listOf("2026-09-10T01:00", "2026-09-11T01:00", "2026-09-12T01:00"), slots.map { it.atUtc() })
    }

    @Test
    fun `recurrence_until stops the series`() {
        val daily =
            rule(
                utc("2026-09-10T01:00"),
                MaintenanceRecurrence.DAILY,
                durationSeconds = 3_600,
                until = utc("2026-09-12T00:00"),
                zone = ZoneId.of("UTC"),
            )

        val slots = MaintenanceRecurrencePlanner.plan(daily, from = utc("2026-09-01T00:00"), until = utc("2026-10-01T00:00"))

        assertEquals(listOf("2026-09-10T01:00", "2026-09-11T01:00"), slots.map { it.atUtc() })
    }

    @Test
    fun `the limit caps the series`() {
        val daily = rule(utc("2026-09-10T01:00"), MaintenanceRecurrence.DAILY, durationSeconds = 3_600, zone = ZoneId.of("UTC"))

        val slots =
            MaintenanceRecurrencePlanner.plan(
                daily,
                from = utc("2026-09-01T00:00"),
                until = utc("2027-09-01T00:00"),
                limit = 5,
            )

        assertEquals(5, slots.size)
    }

    @Test
    fun `a rule with no duration or an inverted range yields nothing`() {
        val zero = rule(utc("2026-09-10T01:00"), MaintenanceRecurrence.DAILY, durationSeconds = 0)
        assertTrue(MaintenanceRecurrencePlanner.plan(zero, from = utc("2026-09-01T00:00"), until = utc("2026-10-01T00:00")).isEmpty())

        val daily = rule(utc("2026-09-10T01:00"), MaintenanceRecurrence.DAILY)
        assertTrue(MaintenanceRecurrencePlanner.plan(daily, from = utc("2026-10-01T00:00"), until = utc("2026-09-01T00:00")).isEmpty())
    }

    private fun MaintenanceRecurrencePlanner.Slot.atUtc(): String = startsAt.atZone(ZoneOffset.UTC).toLocalDateTime().toString()
}
