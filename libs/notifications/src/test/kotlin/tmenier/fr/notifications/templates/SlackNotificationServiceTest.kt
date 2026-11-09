package tmenier.fr.notifications.templates

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.notifications.NotificationDateFormatter
import tmenier.fr.notifications.NotificationHttpClient
import java.time.Duration
import java.time.Instant

class SlackNotificationServiceTest {
    private val http = NotificationHttpClient(Duration.ofSeconds(2), Duration.ofSeconds(2))
    private val service = SlackNotificationService(http, NotificationDateFormatter("UTC"))

    @AfterEach
    fun stop() {
        http.close()
    }

    @Test
    fun `sets a top-level text so push notifications have a preview`() {
        val payload =
            service.buildSlackMessage(
                "Service api - FAILURE",
                "expected \"200\"",
                "#FF0000",
                Instant.parse("2026-10-06T12:00:00Z"),
                ProbeMonitorLogStatus.FAILURE,
            )

        assertTrue(payload.contains("\"text\": \"Service api - FAILURE: expected \\\"200\\\"\""), payload)
        assertTrue(payload.contains("\"fallback\": \"Service api - FAILURE: expected \\\"200\\\"\""), payload)
    }

    @Test
    fun `falls back to the title alone when the message is blank`() {
        val payload =
            service.buildSlackMessage(
                "Service api - SUCCESS",
                "",
                "#00FF00",
                Instant.parse("2026-10-06T12:00:00Z"),
                ProbeMonitorLogStatus.SUCCESS,
            )

        assertTrue(payload.contains("\"text\": \"Service api - SUCCESS\","), payload)
    }
}
