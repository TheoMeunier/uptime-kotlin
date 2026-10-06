package tmenier.fr.notifications

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneOffset

class NotificationDateFormatterTest {
    private val summer = Instant.parse("2026-07-14T10:15:30Z")
    private val winter = Instant.parse("2026-01-14T10:15:30Z")

    @Test
    fun `utc by default label`() {
        assertEquals("14/07/2026 10:15:30 UTC", NotificationDateFormatter("UTC").format(summer))
        assertEquals("14/07/2026 10:15:30 UTC", NotificationDateFormatter("Etc/UTC").format(summer))
    }

    @Test
    fun `named zone follows daylight saving`() {
        val paris = NotificationDateFormatter("Europe/Paris")
        assertEquals("14/07/2026 12:15:30 Europe/Paris (UTC+02:00)", paris.format(summer))
        assertEquals("14/01/2026 11:15:30 Europe/Paris (UTC+01:00)", paris.format(winter))
    }

    @Test
    fun `zone at utc offset zero in winter`() {
        assertEquals("14/01/2026 10:15:30 Europe/London (UTC)", NotificationDateFormatter("Europe/London").format(winter))
    }

    @Test
    fun `fixed offset`() {
        assertEquals("14/07/2026 15:45:30 UTC+05:30", NotificationDateFormatter("+05:30").format(summer))
    }

    @Test
    fun `unknown zone falls back to utc`() {
        val formatter = NotificationDateFormatter("Mars/Olympus")
        assertEquals(ZoneOffset.UTC, formatter.zone)
        assertEquals("14/07/2026 10:15:30 UTC", formatter.format(summer))
    }
}
