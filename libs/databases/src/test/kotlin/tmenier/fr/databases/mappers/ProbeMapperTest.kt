package tmenier.fr.databases.mappers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.StoreProbeDto
import java.time.LocalDateTime
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

    @Test
    fun `redacts encrypted connection string from probe responses`() {
        val entity =
            ProbeMapper
                .toEntity(
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
                                connectionString = "enc:v1:sensitive",
                                host = "localhost/application",
                                query = "SELECT 1",
                            ),
                    ),
                ).apply {
                    createdAt = LocalDateTime.now()
                    updatedAt = LocalDateTime.now()
                }

        val content =
            ProbeMapper
                .toProbeWithNotificationsIdsDto(entity)
                .probe
                .content as ProbeContent.PostgreSql

        assertEquals("", content.connectionString)
        assertEquals("localhost/application", content.host)
    }

    @Test
    fun `can reveal connection string for probe edit responses`() {
        val entity =
            ProbeMapper
                .toEntity(
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
                                connectionString = "enc:v1:sensitive",
                                host = "localhost/application",
                                query = "SELECT 1",
                            ),
                    ),
                ).apply {
                    createdAt = LocalDateTime.now()
                    updatedAt = LocalDateTime.now()
                }

        val content =
            ProbeMapper
                .toProbeWithNotificationsIdsDto(entity) { probeContent ->
                    ProbeContentMapper.revealSecrets(probeContent) { value -> value.replace("enc:v1:", "postgres://") }
                }.probe
                .content as ProbeContent.PostgreSql

        assertEquals("postgres://sensitive", content.connectionString)
        assertEquals("localhost/application", content.host)
    }

    @Test
    fun `redacts encrypted RabbitMQ password from probe responses`() {
        val content =
            ProbeContentMapper.redactSecrets(
                ProbeContent.RabbitMq(
                    managementNodes = "https://rabbitmq.example.com:15672",
                    username = "monitor",
                    password = "enc:v1:sensitive",
                ),
            ) as ProbeContent.RabbitMq

        assertEquals("", content.password)
        assertEquals("monitor", content.username)
    }
}
