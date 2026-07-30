package tmenier.fr.schedulers.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.JdbcSqlServerHealthCheck
import tmenier.fr.schedulers.services.SqlServerHealthCheck
import java.time.LocalDateTime
import java.util.UUID

class ProbeProtocolSqlServerTest {
    @Test
    fun `converts a SQL Server connection string to JDBC without credentials in the URL`() {
        val connection =
            JdbcSqlServerHealthCheck().parseConnectionString(
                "sqlserver://monitor:secret@sql.example.com:1434/application?encrypt=true&trustServerCertificate=true",
            )

        assertEquals(
            "jdbc:sqlserver://sql.example.com:1434;databaseName=application;encrypt=true;trustServerCertificate=true",
            connection.jdbcUrl,
        )
        assertEquals("monitor", connection.username)
        assertEquals("secret", connection.password)
    }

    @Test
    fun `uses SQL Server defaults when port and credentials are omitted`() {
        val connection =
            JdbcSqlServerHealthCheck().parseConnectionString(
                "mssql://localhost/application",
            )

        assertEquals(
            "jdbc:sqlserver://localhost:1433;databaseName=application",
            connection.jdbcUrl,
        )
        assertEquals(null, connection.username)
        assertEquals(null, connection.password)
    }

    @Test
    fun `returns success when SQL Server accepts the health query`() {
        val executor = ProbeProtocolSqlServer(SqlServerHealthCheck { _, _ -> })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertTrue(result.message.startsWith("Microsoft SQL Server connection successful"))
    }

    @Test
    fun `returns warning when a retry remains`() {
        val executor =
            ProbeProtocolSqlServer(
                SqlServerHealthCheck { _, _ -> error("connection refused") },
            )

        val result = executor.execute(probe(), content(), false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
        assertTrue(result.message.contains("connection refused"))
    }

    @Test
    fun `returns failure on the last attempt`() {
        val executor =
            ProbeProtocolSqlServer(
                SqlServerHealthCheck { _, _ -> error("authentication failed") },
            )

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("authentication failed"))
    }

    private fun content() =
        ProbeContent.SqlServer(
            connectionString = "sqlserver://monitor:secret@sql.example.com:1433/application",
            host = "sql.example.com:1433/application",
            query = "SELECT 1",
        )

    private fun probe() =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "SQL Server test",
            interval = 60,
            timeout = 5,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.SQLSERVER,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
}
