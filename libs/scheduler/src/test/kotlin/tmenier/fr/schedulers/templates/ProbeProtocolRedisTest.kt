package tmenier.fr.schedulers.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.RedisHealthCheck
import tmenier.fr.schedulers.services.SocketRedisHealthCheck
import java.time.LocalDateTime
import java.util.UUID

class ProbeProtocolRedisTest {
    private val healthCheck =
        SocketRedisHealthCheck(EncryptionService("0123456789abcdef0123456789abcdef"))

    @Test
    fun `parses a Redis connection string with default values`() {
        val connection =
            healthCheck.parseConnectionString(
                "redis://localhost",
            )

        assertEquals("localhost", connection.host)
        assertEquals(6379, connection.port)
        assertEquals(0, connection.database)
        assertEquals(null, connection.username)
        assertEquals(null, connection.password)
        assertEquals(false, connection.tls)
    }

    @Test
    fun `parses Redis TLS credentials and database`() {
        val connection =
            healthCheck.parseConnectionString(
                "rediss://monitor:secret@redis.example.com:6380/2",
            )

        assertEquals("redis.example.com", connection.host)
        assertEquals(6380, connection.port)
        assertEquals(2, connection.database)
        assertEquals("monitor", connection.username)
        assertEquals("secret", connection.password)
        assertEquals(true, connection.tls)
    }

    @Test
    fun `parses a Redis command with quoted arguments`() {
        val arguments =
            healthCheck.parseCommand(
                """SET "health key" 'all good'""",
            )

        assertEquals(listOf("SET", "health key", "all good"), arguments)
    }

    @Test
    fun `returns success when Redis accepts the command`() {
        val executor = ProbeProtocolRedis(RedisHealthCheck { _, _ -> })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertTrue(result.message.startsWith("Redis command successful"))
    }

    @Test
    fun `returns warning when a retry remains`() {
        val executor =
            ProbeProtocolRedis(
                RedisHealthCheck { _, _ -> error("connection refused") },
            )

        val result = executor.execute(probe(), content(), false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
        assertTrue(result.message.contains("connection refused"))
    }

    @Test
    fun `returns failure on the last attempt`() {
        val executor =
            ProbeProtocolRedis(
                RedisHealthCheck { _, _ -> error("authentication failed") },
            )

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("authentication failed"))
    }

    private fun content() =
        ProbeContent.Redis(
            connectionString = "redis://monitor:secret@redis.example.com:6379/0",
            host = "redis.example.com:6379/0",
            command = "PING",
        )

    private fun probe() =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "Redis test",
            interval = 60,
            timeout = 5,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.REDIS,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
}
