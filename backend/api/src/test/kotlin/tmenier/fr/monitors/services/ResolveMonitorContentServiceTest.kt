package tmenier.fr.monitors.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.common.enums.monitors.SmtpSecurity
import tmenier.fr.monitors.requests.ValidProbeProtocolMySqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPostgreSqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolRabbitMqRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolRedisRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolSmtpRequest
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

    @Test
    fun `resolves Redis connection string with its database`() {
        val content =
            ResolveMonitorContentService().resolve(
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
            ResolveMonitorContentService().resolve(
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
            ResolveMonitorContentService().resolve(
                ValidProbeProtocolRabbitMqRequest(
                    managementNodes = "https://rabbitmq.example.com:15672",
                    username = "monitor",
                    password = "secret",
                ),
            ) as ProbeContent.RabbitMq

        assertEquals("https://rabbitmq.example.com:15672", content.managementNodes)
        assertEquals("monitor", content.username)
        assertEquals("secret", content.password)
    }
}
