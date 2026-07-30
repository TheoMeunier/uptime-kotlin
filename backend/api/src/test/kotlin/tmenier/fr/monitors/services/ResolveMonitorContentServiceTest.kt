package tmenier.fr.monitors.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.monitors.requests.ValidProbeProtocolMySqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPostgreSqlRequest

class ResolveMonitorContentServiceTest {
    @Test
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
