package tmenier.fr.notifications

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class NotificationDateFormatterTest {
    private val summer = Instant.parse("2026-07-14T10:15:30Z")
    private val winter = Instant.parse("2026-01-14T10:15:30Z")

    private fun custom(
        zone: String,
        pattern: String,
    ) = NotificationDateFormatter(zone, pattern)

    @Test
    fun `default pattern in utc`() {
        assertEquals("14/07/2026 10:15:30 UTC", NotificationDateFormatter("UTC").format(summer))
    }

    @Test
    fun `default pattern follows daylight saving`() {
        val paris = NotificationDateFormatter("Europe/Paris")
        assertEquals("14/07/2026 12:15:30 Europe/Paris", paris.format(summer))
        assertEquals("14/01/2026 11:15:30 Europe/Paris", paris.format(winter))
    }

    @Test
    fun `default pattern with a fixed offset`() {
        assertEquals("14/07/2026 15:45:30 +05:30", NotificationDateFormatter("+05:30").format(summer))
    }

    @Test
    fun `unknown zone falls back to utc`() {
        val formatter = NotificationDateFormatter("Mars/Olympus")
        assertEquals(NotificationDateFormatter.UTC, formatter.zone)
        assertEquals("14/07/2026 10:15:30 UTC", formatter.format(summer))
    }

    @Test
    fun `us 12-hour clock without zone`() {
        assertEquals("07/14/2026 10:15:30 AM", custom("UTC", "MM/dd/yyyy hh:mm:ss a").format(summer))
        assertEquals("07/14/2026 06:15:30 PM", custom("Asia/Manila", "MM/dd/yyyy hh:mm:ss a").format(summer))
    }

    @Test
    fun `documented examples`() {
        assertEquals("2026-07-14 12:15:30 +02:00", custom("Europe/Paris", "yyyy-MM-dd HH:mm:ss xxx").format(summer))
        assertEquals("14/07/2026 12:15 CEST", custom("Europe/Paris", "dd/MM/yyyy HH:mm z").format(summer))
        assertEquals("Tue 14 Jul 2026, 12:15", custom("Europe/Paris", "EEE d MMM yyyy, HH:mm").format(summer))
        assertEquals("14.07.2026 12:15", custom("Europe/Paris", "dd.MM.yyyy HH:mm").format(summer))
    }

    @Test
    fun `blank or invalid pattern falls back to the default`() {
        assertEquals("14/07/2026 10:15:30 UTC", custom("UTC", "  ").format(summer))
        assertEquals("14/07/2026 10:15:30 UTC", custom("UTC", "dd/MM/yyyy bb").format(summer))
        assertEquals("14/07/2026 10:15:30 UTC", custom("UTC", "'unterminated").format(summer))
    }
}
