package tmenier.fr.schedulers.templates

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.schedulers.services.JdbcOracleHealthCheck
import tmenier.fr.schedulers.services.OracleHealthCheck
import java.time.Instant
import java.util.UUID

class ProbeProtocolOracleTest {
    private val encryptionService = EncryptionService("0123456789abcdef0123456789abcdef")
    private val healthCheck = JdbcOracleHealthCheck(encryptionService)

    @Test
    fun `converts an Oracle connection string to an Easy Connect URL without credentials`() {
        val connection =
            healthCheck.parseConnectionString(
                "oracle://monitor:s%40cret@db.example.com:1522/ORCLPDB1.example.com",
            )

        assertEquals("jdbc:oracle:thin:@//db.example.com:1522/ORCLPDB1.example.com", connection.jdbcUrl)
        assertEquals("monitor", connection.username)
        assertEquals("s@cret", connection.password)
        assertFalse(connection.jdbcUrl.contains("cret"))
    }

    @Test
    fun `uses Oracle defaults when port and credentials are omitted`() {
        val connection = healthCheck.parseConnectionString(encryptionService.encrypt("oracle://localhost/FREEPDB1"))

        assertEquals("jdbc:oracle:thin:@//localhost:1521/FREEPDB1", connection.jdbcUrl)
        assertNull(connection.username)
        assertNull(connection.password)
        assertTrue(connection.properties.isEmpty())
    }

    @Test
    fun `wraps IPv6 hosts in brackets once`() {
        val connection = healthCheck.parseConnectionString("oracle://[::1]:1521/FREEPDB1")

        assertEquals("jdbc:oracle:thin:@//[::1]:1521/FREEPDB1", connection.jdbcUrl)
    }

    @Test
    fun `passes query parameters as driver properties`() {
        val connection =
            healthCheck.parseConnectionString(
                "oracle://localhost/FREEPDB1?internal_logon=sysdba&oracle.net.ssl_server_dn_match=true",
            )

        assertEquals("jdbc:oracle:thin:@//localhost:1521/FREEPDB1", connection.jdbcUrl)
        assertEquals(
            mapOf("internal_logon" to "sysdba", "oracle.net.ssl_server_dn_match" to "true"),
            connection.properties,
        )
    }

    @Test
    fun `rejects invalid connection strings`() {
        assertThrows<IllegalArgumentException> { healthCheck.parseConnectionString("postgres://localhost/app") }
        assertThrows<IllegalArgumentException> { healthCheck.parseConnectionString("oracle://localhost") }
        assertThrows<IllegalArgumentException> { healthCheck.parseConnectionString("oracle://localhost/") }
    }

    @Test
    fun `strips the trailing semicolon of SQL statements but keeps PL SQL blocks intact`() {
        assertEquals("SELECT 1 FROM DUAL", healthCheck.normalizeQuery("  SELECT 1 FROM DUAL;  "))
        assertEquals("SELECT 1 FROM DUAL", healthCheck.normalizeQuery(" "))
        assertEquals("BEGIN NULL; END;", healthCheck.normalizeQuery("BEGIN NULL; END;"))
        assertEquals("declare x number; begin x := 1; end;", healthCheck.normalizeQuery("declare x number; begin x := 1; end;"))
    }

    @Test
    fun `returns success when Oracle accepts the health query`() {
        val executor = ProbeProtocolOracle(OracleHealthCheck { _, _ -> })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertTrue(result.message.startsWith("Oracle Database connection successful"))
    }

    @Test
    fun `returns warning when a retry remains`() {
        val executor = ProbeProtocolOracle(OracleHealthCheck { _, _ -> error("ORA-12541: Cannot connect. No listener") })

        val result = executor.execute(probe(), content(), false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
        assertTrue(result.message.contains("ORA-12541"))
    }

    @Test
    fun `returns failure on the last attempt`() {
        val executor = ProbeProtocolOracle(OracleHealthCheck { _, _ -> error("ORA-01017: invalid username/password") })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("ORA-01017"))
    }

    @Test
    fun `passes the probe timeout to the health check`() {
        var received: Int? = null
        val executor = ProbeProtocolOracle(OracleHealthCheck { _, timeoutSeconds -> received = timeoutSeconds })

        executor.execute(probe(timeout = 12), content(), true)

        assertEquals(12, received)
    }

    private fun content() =
        ProbeContent.Oracle(
            connectionString = "oracle://monitor:secret@db.example.com:1521/FREEPDB1",
            host = "db.example.com:1521/FREEPDB1",
        )

    private fun probe(timeout: Int = 5) =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "Oracle test",
            interval = 60,
            timeout = timeout,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.ORACLE,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
}
