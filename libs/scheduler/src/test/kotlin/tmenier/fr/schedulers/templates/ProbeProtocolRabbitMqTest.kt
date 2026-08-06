package tmenier.fr.schedulers.templates

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.HttpRabbitMqHealthCheck
import tmenier.fr.schedulers.services.RabbitMqHealthCheck
import java.net.InetSocketAddress
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

class ProbeProtocolRabbitMqTest {
    private val encryptionService = EncryptionService("0123456789abcdef0123456789abcdef")
    private val healthCheck = HttpRabbitMqHealthCheck(encryptionService)

    @Test
    fun `checks the alarms endpoint with basic authentication`() {
        val authorization = AtomicReference<String>()
        val server = HttpServer.create(InetSocketAddress(0), 0)
        server.createContext("/api/health/checks/alarms") { exchange ->
            authorization.set(exchange.requestHeaders.getFirst("Authorization"))
            val body = "{}".toByteArray()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()

        try {
            healthCheck.check(
                content().copy(
                    managementNodes = "http://127.0.0.1:${server.address.port}",
                    password = encryptionService.encrypt("secret"),
                ),
                timeoutSeconds = 2,
            )

            assertEquals("Basic bW9uaXRvcjpzZWNyZXQ=", authorization.get())
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun `builds a health endpoint with a management path prefix`() {
        val uri =
            healthCheck.healthUri(
                "https://rabbitmq.example.com:15671/management/",
            )

        assertEquals(
            "https://rabbitmq.example.com:15671/management/api/health/checks/alarms",
            uri.toString(),
        )
    }

    @Test
    fun `returns success when RabbitMQ reports no alarms`() {
        val executor = ProbeProtocolRabbitMq(RabbitMqHealthCheck { _, _ -> })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertTrue(result.message.startsWith("RabbitMQ management nodes healthy"))
    }

    @Test
    fun `returns warning when a retry remains`() {
        val executor =
            ProbeProtocolRabbitMq(
                RabbitMqHealthCheck { _, _ -> error("HTTP 503") },
            )

        val result = executor.execute(probe(), content(), false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
        assertTrue(result.message.contains("HTTP 503"))
    }

    @Test
    fun `returns failure on the last attempt`() {
        val executor =
            ProbeProtocolRabbitMq(
                RabbitMqHealthCheck { _, _ -> error("authentication failed") },
            )

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("authentication failed"))
    }

    private fun content() =
        ProbeContent.RabbitMq(
            managementNodes = "https://rabbitmq.example.com:15672",
            username = "monitor",
            password = "secret",
        )

    private fun probe() =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "RabbitMQ test",
            interval = 60,
            timeout = 5,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.RABBITMQ,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
}
