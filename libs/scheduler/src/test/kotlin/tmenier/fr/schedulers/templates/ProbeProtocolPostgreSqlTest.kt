package tmenier.fr.schedulers.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.JdbcPostgreSqlHealthCheck
import tmenier.fr.schedulers.services.PostgreSqlHealthCheck
import java.time.LocalDateTime
import java.util.UUID

class ProbeProtocolPostgreSqlTest {
    @Test
    fun `accepts a PostgreSQL connection string without credentials`() {
        val connection =
            JdbcPostgreSqlHealthCheck().parseConnectionString(
                "postgres://localhost:5432/application",
            )

        assertEquals("jdbc:postgresql://localhost:5432/application", connection.jdbcUrl)
        assertEquals(null, connection.username)
        assertEquals(null, connection.password)
    }

    @Test
    fun `converts a PostgreSQL connection string to JDBC without credentials in the URL`() {
        val connection =
            JdbcPostgreSqlHealthCheck().parseConnectionString(
                "postgres://monitor:secret@postgres.example.com:5433/application?sslmode=require",
            )

        assertEquals(
            "jdbc:postgresql://postgres.example.com:5433/application?sslmode=require",
            connection.jdbcUrl,
        )
        assertEquals("monitor", connection.username)
        assertEquals("secret", connection.password)
    }

    @Test
    fun `returns success when PostgreSQL accepts the health query`() {
        val executor = ProbeProtocolPostgreSql(PostgreSqlHealthCheck { _, _ -> })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertTrue(result.message.startsWith("PostgreSQL connection successful"))
    }

    @Test
    fun `returns warning when a retry remains`() {
        val executor =
            ProbeProtocolPostgreSql(
                PostgreSqlHealthCheck { _, _ -> error("connection refused") },
            )

        val result = executor.execute(probe(), content(), false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
        assertTrue(result.message.contains("connection refused"))
    }

    @Test
    fun `returns failure on the last attempt`() {
        val executor =
            ProbeProtocolPostgreSql(
                PostgreSqlHealthCheck { _, _ -> error("authentication failed") },
            )

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("authentication failed"))
    }

    private fun content() =
        ProbeContent.PostgreSql(
            connectionString = "postgres://monitor:secret@postgres.example.com:5432/application",
            host = "postgres.example.com:5432/application",
            query = "SELECT 1",
        )

    private fun probe() =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "PostgreSQL test",
            interval = 60,
            timeout = 5,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.POSTGRESQL,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
}
