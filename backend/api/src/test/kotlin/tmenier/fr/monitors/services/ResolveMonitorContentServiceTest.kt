package tmenier.fr.monitors.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.monitors.requests.ValidProbeProtocolMySqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPostgreSqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolSqlServerRequest

class ResolveMonitorContentServiceTest {
    @Test
    fun `serializes SQL Server protocol with its API value`() {
        val objectMapper = ObjectMapper()

        assertEquals(
            "\"MICROSOFT SQL SERVER\"",
            objectMapper.writeValueAsString(ProbeProtocol.SQLSERVER),
        )
        assertEquals(
            ProbeProtocol.SQLSERVER,
            objectMapper.readValue("\"MICROSOFT SQL SERVER\"", ProbeProtocol::class.java),
        )
    }

    fun `serializes MySQL protocol with the MySQL MariaDB API value`() {
        val objectMapper = ObjectMapper()

        assertEquals("\"MYSQL / MARIADB\"", objectMapper.writeValueAsString(ProbeProtocol.MYSQL))
        assertEquals(
            ProbeProtocol.MYSQL,
            objectMapper.readValue("\"MYSQL / MARIADB\"", ProbeProtocol::class.java),
        )
    }

    @Test
    fun `resolves PostgreSQL connection string without credentials`() {
        val content =
            ResolveMonitorContentService().resolve(
                ValidProbeProtocolPostgreSqlRequest(
                    connectionString = "postgres://localhost:5432/application",
                    query = "SELECT 1",
                ),
            ) as ProbeContent.PostgreSql

        assertEquals("localhost:5432/application", content.host)
    }

    @Test
    fun `resolves SQL Server connection string without credentials`() {
        val content =
            ResolveMonitorContentService().resolve(
                ValidProbeProtocolSqlServerRequest(
                    connectionString = "sqlserver://localhost/application",
                    query = "SELECT 1",
                ),
            ) as ProbeContent.SqlServer

        assertEquals("localhost:1433/application", content.host)
    }

    fun `resolves MariaDB connection string without credentials`() {
        val content =
            ResolveMonitorContentService().resolve(
                ValidProbeProtocolMySqlRequest(
                    connectionString = "mariadb://localhost/application",
                    query = "SELECT 1",
                ),
            ) as ProbeContent.MySql

        assertEquals("localhost:3306/application", content.host)
    }
}
