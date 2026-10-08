package tmenier.fr.notifications.templates

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.HttpMethodEnum
import tmenier.fr.databases.dtos.NotificationContent
import tmenier.fr.notifications.NotificationDateFormatter
import tmenier.fr.notifications.NotificationHttpClient
import java.net.InetSocketAddress
import java.net.http.HttpTimeoutException
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class NotificationHttpTimeoutTest {
    private lateinit var server: HttpServer
    private val release = CountDownLatch(1)
    private val http = NotificationHttpClient(Duration.ofSeconds(2), Duration.ofMillis(300))
    private lateinit var url: String
    private val encryption = EncryptionService("0123456789abcdef0123456789abcdef")

    @BeforeEach
    fun startHangingServer() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.executor = Executors.newCachedThreadPool()
        server.createContext("/") { exchange ->
            release.await()
            exchange.close()
        }
        server.start()
        url = "http://127.0.0.1:${server.address.port}/hook"
    }

    @AfterEach
    fun stop() {
        release.countDown()
        server.stop(0)
        http.close()
    }

    private fun assertTimesOut(send: () -> Unit) {
        val startedAt = System.nanoTime()
        assertThrows<HttpTimeoutException> { send() }
        val elapsed = Duration.ofNanos(System.nanoTime() - startedAt)
        assertTrue(elapsed < Duration.ofSeconds(5), "took $elapsed")
    }

    @Test
    fun `discord gives up on a receiver that never answers`() =
        assertTimesOut { DiscordNotificationService(http).sendTest(NotificationContent.Discord(url, null)) }

    @Test
    fun `slack gives up on a receiver that never answers`() =
        assertTimesOut {
            SlackNotificationService(http, NotificationDateFormatter("UTC")).sendTest(NotificationContent.Slack(url, null))
        }

    @Test
    fun `teams gives up on a receiver that never answers`() =
        assertTimesOut {
            TeamsNotificationService(http, NotificationDateFormatter("UTC")).sendTest(NotificationContent.Teams(url, null))
        }

    @Test
    fun `telegram gives up on a receiver that never answers`() =
        assertTimesOut {
            TelegramNotificationService(http, NotificationDateFormatter("UTC"), encryption, "http://127.0.0.1:${server.address.port}")
                .sendTest(NotificationContent.Telegram("1:token", "42"))
        }

    @Test
    fun `ntfy gives up on a receiver that never answers`() =
        assertTimesOut {
            NtfyNotificationService(http, NotificationDateFormatter("UTC"), encryption).sendTest(NotificationContent.Ntfy(url, "alerts"))
        }

    @Test
    fun `gotify gives up on a receiver that never answers`() =
        assertTimesOut {
            GotifyNotificationService(http, NotificationDateFormatter("UTC"), encryption).sendTest(NotificationContent.Gotify(url, "token"))
        }

    @Test
    fun `generic webhook gives up on a receiver that never answers`() =
        assertTimesOut {
            WebhookNotificationService(http).sendTest(NotificationContent.Webhook(HttpMethodEnum.POST, url))
        }

    @Test
    fun `the request builder carries the request timeout`() {
        val request = http.request(url).GET().build()
        assertEquals(Duration.ofMillis(300), request.timeout().get())
    }
}
