package tmenier.fr.databases.mappers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.StoreProbeDto
import java.util.UUID

class ProbeMapperTest {
    @Test
    fun `new probe has an empty regions order instead of null`() {
        val entity =
            ProbeMapper.toEntity(
                StoreProbeDto(
                    id = UUID.randomUUID(),
                    name = "PostgreSQL monitor",
                    interval = 60,
                    intervalRetry = 60,
                    retry = 3,
                    protocol = ProbeProtocol.POSTGRESQL,
                    enabled = true,
                    description = null,
                    content =
                        ProbeContent.PostgreSql(
                            connectionString = "postgres://monitor:secret@localhost/application",
                            host = "localhost/application",
                            query = "SELECT 1",
                        ),
                ),
            )

        val regionsOrder = requireNotNull(entity.regionsOrder)
        assertTrue(regionsOrder.isArray)
        assertEquals(0, regionsOrder.size())
    }
}
