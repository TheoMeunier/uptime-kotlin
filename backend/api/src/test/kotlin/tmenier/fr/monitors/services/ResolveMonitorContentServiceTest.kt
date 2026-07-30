package tmenier.fr.monitors.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.monitors.requests.ValidProbeProtocolPostgreSqlRequest

class ResolveMonitorContentServiceTest {
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
}
