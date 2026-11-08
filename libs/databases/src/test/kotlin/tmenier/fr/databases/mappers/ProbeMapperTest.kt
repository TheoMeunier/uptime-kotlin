package tmenier.fr.databases.mappers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.HttpCodeEnum
import tmenier.fr.common.enums.monitors.HttpMethodEnum
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.common.enums.monitors.SmtpSecurity
import tmenier.fr.databases.dtos.StoreProbeDto
import tmenier.fr.databases.entities.ProbePushStateEntity
import java.time.Instant
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
                    timeout = 5,
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
    fun `keeps the configured timeout on a new probe`() {
        val entity =
            ProbeMapper.toEntity(
                StoreProbeDto(
                    id = UUID.randomUUID(),
                    name = "Slow API",
                    interval = 60,
                    timeout = 30,
                    intervalRetry = 60,
                    retry = 3,
                    protocol = ProbeProtocol.SMTP,
                    enabled = true,
                    description = null,
                    content = ProbeContent.Smtp(hostname = "smtp.example.com", port = 587, security = SmtpSecurity.STARTTLS),
                ),
            )

        assertEquals(30, entity.timeout)
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
                        timeout = 5,
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
                    createdAt = Instant.now()
                    updatedAt = Instant.now()
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
                        timeout = 5,
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
                    createdAt = Instant.now()
                    updatedAt = Instant.now()
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

    @Test
    fun `redacts HTTP headers, body and credentials from probe responses`() {
        val content =
            ProbeContentMapper.redactSecrets(
                ProbeContent.Http(
                    url = "https://api.example.com/health",
                    notificationCertified = false,
                    ignoreCertificateErrors = false,
                    httpCodeAllowed = listOf(HttpCodeEnum.SUCCESS),
                    method = HttpMethodEnum.POST,
                    headers = mapOf("Authorization" to "Bearer SENTINEL_HEADER", "X-Custom" to "SENTINEL_CUSTOM"),
                    body = """{"apiKey":"SENTINEL_BODY"}""",
                    authentication =
                        ProbeContent.HttpAuthentication(
                            type = ProbeContent.HttpAuthenticationType.BASIC,
                            username = "SENTINEL_USER",
                            password = "SENTINEL_PASSWORD",
                        ),
                    steps =
                        listOf(
                            ProbeContent.HttpStep(
                                name = "login",
                                url = "https://api.example.com/login",
                                headers = mapOf("Cookie" to "SENTINEL_STEP_HEADER"),
                                body = "SENTINEL_STEP_BODY",
                                authentication =
                                    ProbeContent.HttpAuthentication(
                                        type = ProbeContent.HttpAuthenticationType.BEARER,
                                        token = "SENTINEL_STEP_TOKEN",
                                    ),
                            ),
                        ),
                ),
            ) as ProbeContent.Http

        assertFalse(content.toString().contains("SENTINEL"), "Secret leaked: $content")
        assertEquals(setOf("Authorization", "X-Custom"), content.headers.keys)
        assertTrue(content.headers.values.all { it == ProbeContentMapper.REDACTED })
        assertEquals(ProbeContent.HttpAuthenticationType.BASIC, content.authentication?.type)
        assertEquals(ProbeContent.HttpAuthenticationType.BEARER, content.steps.single().authentication?.type)
        assertEquals("https://api.example.com/health", content.url)
        assertEquals(HttpMethodEnum.POST, content.method)
    }

    @Test
    fun `only reveals the push token when asked to`() {
        val entity =
            ProbeMapper
                .toEntity(
                    StoreProbeDto(
                        id = UUID.randomUUID(),
                        name = "Nightly backup",
                        interval = 60,
                        timeout = 5,
                        intervalRetry = 60,
                        retry = 3,
                        protocol = ProbeProtocol.PUSH,
                        enabled = true,
                        description = null,
                        content = ProbeContent.Push(periodSeconds = 86_400, graceSeconds = 600),
                    ),
                ).apply {
                    createdAt = Instant.now()
                    updatedAt = Instant.now()
                }
        val push =
            ProbePushStateEntity().apply {
                probeId = entity.id
                token = "SENTINEL_PUSH_TOKEN"
                armedAt = Instant.now()
                pushCount = 3
            }

        val hidden = ProbeMapper.toShowDto(entity = entity, monitors = emptyList(), push = push).push
        val revealed = ProbeMapper.toShowDto(entity = entity, monitors = emptyList(), push = push, revealPushToken = true).push

        assertNull(hidden?.token)
        assertEquals(3L, hidden?.pushCount)
        assertEquals("SENTINEL_PUSH_TOKEN", revealed?.token)
    }
}
