package tmenier.fr.monitors.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.enums.monitors.ProbeMonitorLogStatus
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.common.enums.monitors.SmtpSecurity
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.monitors.requests.OnCreate
import tmenier.fr.monitors.requests.OnUpdate
import tmenier.fr.monitors.requests.ValidProbeProtocolMySqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPostgreSqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolRabbitMqRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolRedisRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolSmtpRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolSqlServerRequest
import java.time.LocalDateTime
import java.util.UUID

class ResolveMonitorContentServiceTest {
    private val encryptionService = EncryptionService("0123456789abcdef0123456789abcdef")
    private val service = ResolveMonitorContentService(encryptionService)

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
            service.resolve(
                ValidProbeProtocolPostgreSqlRequest(
                    connectionString = "postgres://localhost:5432/application",
                    query = "SELECT 1",
                ),
            ) as ProbeContent.PostgreSql

        assertEquals("localhost:5432/application", content.host)
    }

    @Test
    fun `encrypts PostgreSQL connection string before persistence`() {
        val plainText = "postgres://monitor:secret@localhost:5432/application"

        val content =
            service.resolve(
                ValidProbeProtocolPostgreSqlRequest(
                    connectionString = plainText,
                    query = "SELECT 1",
                ),
            ) as ProbeContent.PostgreSql

        assertNotEquals(plainText, content.connectionString)
        assertEquals(plainText, encryptionService.decrypt(content.connectionString))
        assertEquals("localhost:5432/application", content.host)
    }

    @Test
    fun `preserves encrypted connection string when update leaves it blank`() {
        val plainText = "postgres://monitor:secret@localhost:5432/application"
        val encrypted = encryptionService.encrypt(plainText)
        val existingProbe =
            ProbeDTO(
                id = UUID.randomUUID(),
                name = "PostgreSQL",
                interval = 60,
                timeout = 5,
                retry = 1,
                intervalRetry = 10,
                enabled = true,
                protocol = ProbeProtocol.POSTGRESQL,
                description = null,
                lastRun = null,
                status = ProbeMonitorLogStatus.SUCCESS,
                content =
                    ProbeContent.PostgreSql(
                        connectionString = encrypted,
                        host = "localhost:5432/application",
                        query = "SELECT 1",
                    ),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val content =
            service.resolve(
                ValidProbeProtocolPostgreSqlRequest(
                    connectionString = "",
                    query = "SELECT current_timestamp",
                ),
                existingProbe,
            ) as ProbeContent.PostgreSql

        assertEquals(encrypted, content.connectionString)
        assertEquals("localhost:5432/application", content.host)
        assertEquals("SELECT current_timestamp", content.query)
    }

    @Test
    fun `requires a connection string on creation`() {
        val request =
            ValidProbeProtocolPostgreSqlRequest(
                connectionString = "",
                query = "SELECT 1",
            )
        val violations =
            Validation
                .buildDefaultValidatorFactory()
                .validator
                .validate(request, OnCreate::class.java)

        assertTrue(violations.any { it.propertyPath.toString() == "connectionString" })
    }

    @Test
    fun `allows a blank connection string on update to preserve the secret`() {
        val request =
            ValidProbeProtocolPostgreSqlRequest(
                connectionString = "",
                query = "SELECT 1",
            )
        val violations =
            Validation
                .buildDefaultValidatorFactory()
                .validator
                .validate(request, OnUpdate::class.java)

        assertFalse(violations.any { it.propertyPath.toString() == "connectionString" })
    }

    @Test
    fun `replaces the encrypted connection string when update supplies a new value`() {
        val previous = "postgres://old:secret@old-host:5432/application"
        val replacement = "postgres://new:secret@new-host:5433/replacement"
        val existingProbe =
            ProbeDTO(
                id = UUID.randomUUID(),
                name = "PostgreSQL",
                interval = 60,
                timeout = 5,
                retry = 1,
                intervalRetry = 10,
                enabled = true,
                protocol = ProbeProtocol.POSTGRESQL,
                description = null,
                lastRun = null,
                status = ProbeMonitorLogStatus.SUCCESS,
                content =
                    ProbeContent.PostgreSql(
                        connectionString = encryptionService.encrypt(previous),
                        host = "old-host:5432/application",
                        query = "SELECT 1",
                    ),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val content =
            service.resolve(
                ValidProbeProtocolPostgreSqlRequest(
                    connectionString = replacement,
                    query = "SELECT current_timestamp",
                ),
                existingProbe,
            ) as ProbeContent.PostgreSql

        assertEquals(replacement, encryptionService.decrypt(content.connectionString))
        assertEquals("new-host:5433/replacement", content.host)
    }

    @Test
    fun `resolves SQL Server connection string without credentials`() {
        val content =
            service.resolve(
                ValidProbeProtocolSqlServerRequest(
                    connectionString = "sqlserver://localhost/application",
                    query = "SELECT 1",
                ),
            ) as ProbeContent.SqlServer

        assertEquals("localhost:1433/application", content.host)
    }

    @Test
    fun `resolves MariaDB connection string without credentials`() {
        val content =
            service.resolve(
                ValidProbeProtocolMySqlRequest(
                    connectionString = "mariadb://localhost/application",
                    query = "SELECT 1",
                ),
            ) as ProbeContent.MySql

        assertEquals("localhost:3306/application", content.host)
    }

    @Test
    fun `resolves Redis connection string with its database`() {
        val content =
            service.resolve(
                ValidProbeProtocolRedisRequest(
                    connectionString = "redis://localhost/2",
                    command = "PING",
                ),
            ) as ProbeContent.Redis

        assertEquals("localhost:6379/2", content.host)
    }

    @Test
    fun `resolves SMTP server settings`() {
        val content =
            service.resolve(
                ValidProbeProtocolSmtpRequest(
                    hostname = "smtp.example.com",
                    port = 587,
                    security = SmtpSecurity.STARTTLS,
                ),
            ) as ProbeContent.Smtp

        assertEquals("smtp.example.com", content.hostname)
        assertEquals(587, content.port)
        assertEquals(SmtpSecurity.STARTTLS, content.security)
    }

    @Test
    fun `resolves RabbitMQ management settings`() {
        val content =
            service.resolve(
                ValidProbeProtocolRabbitMqRequest(
                    managementNodes = "https://rabbitmq.example.com:15672",
                    username = "monitor",
                    password = "secret",
                ),
            ) as ProbeContent.RabbitMq

        assertEquals("https://rabbitmq.example.com:15672", content.managementNodes)
        assertEquals("monitor", content.username)
        assertNotEquals("secret", content.password)
        assertEquals("secret", encryptionService.decrypt(content.password))
    }

    @Test
    fun `preserves encrypted RabbitMQ password when update leaves it blank`() {
        val encrypted = encryptionService.encrypt("secret")
        val existingProbe =
            ProbeDTO(
                id = UUID.randomUUID(),
                name = "RabbitMQ",
                interval = 60,
                timeout = 5,
                retry = 1,
                intervalRetry = 10,
                enabled = true,
                protocol = ProbeProtocol.RABBITMQ,
                description = null,
                lastRun = null,
                status = ProbeMonitorLogStatus.SUCCESS,
                content =
                    ProbeContent.RabbitMq(
                        managementNodes = "https://rabbitmq.example.com:15672",
                        username = "monitor",
                        password = encrypted,
                    ),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        val content =
            service.resolve(
                ValidProbeProtocolRabbitMqRequest(
                    managementNodes = "https://rabbitmq.example.com:15672",
                    username = "monitor",
                    password = "",
                ),
                existingProbe,
            ) as ProbeContent.RabbitMq

        assertEquals(encrypted, content.password)
    }
}
