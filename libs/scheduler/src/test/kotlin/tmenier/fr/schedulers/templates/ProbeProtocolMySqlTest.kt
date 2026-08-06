package tmenier.fr.schedulers.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.JdbcMySqlHealthCheck
import tmenier.fr.schedulers.services.MySqlHealthCheck
import java.time.LocalDateTime
import java.util.UUID

class ProbeProtocolMySqlTest {
    private val healthCheck =
        JdbcMySqlHealthCheck(EncryptionService("0123456789abcdef0123456789abcdef"))

    @Test
    fun `converts a MySQL connection string to JDBC without credentials in the URL`() {
        val connection =
            healthCheck.parseConnectionString(
                "mysql://monitor:secret@mysql.example.com:3307/application?useSSL=true",
            )

        assertEquals(
            "jdbc:mysql://mysql.example.com:3307/application?useSSL=true",
            connection.jdbcUrl,
        )
        assertEquals("monitor", connection.username)
        assertEquals("secret", connection.password)
    }

    @Test
    fun `converts a MariaDB connection string using the MariaDB driver`() {
        val connection =
            healthCheck.parseConnectionString(
                "mariadb://localhost/application",
            )

        assertEquals("jdbc:mariadb://localhost:3306/application", connection.jdbcUrl)
        assertEquals(null, connection.username)
        assertEquals(null, connection.password)
    }

    @Test
    fun `returns success when MySQL accepts the health query`() {
        val executor = ProbeProtocolMySql(MySqlHealthCheck { _, _ -> })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertTrue(result.message.startsWith("MySQL/MariaDB connection successful"))
    }

    @Test
    fun `returns warning when a retry remains`() {
        val executor =
            ProbeProtocolMySql(
                MySqlHealthCheck { _, _ -> error("connection refused") },
            )

        val result = executor.execute(probe(), content(), false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
        assertTrue(result.message.contains("connection refused"))
    }

    @Test
    fun `returns failure on the last attempt`() {
        val executor =
            ProbeProtocolMySql(
                MySqlHealthCheck { _, _ -> error("authentication failed") },
            )

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("authentication failed"))
    }

    private fun content() =
        ProbeContent.MySql(
            connectionString = "mysql://monitor:secret@mysql.example.com:3306/application",
            host = "mysql.example.com:3306/application",
            query = "SELECT 1",
        )

    private fun probe() =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "MySQL test",
            interval = 60,
            timeout = 5,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.MYSQL,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
}
