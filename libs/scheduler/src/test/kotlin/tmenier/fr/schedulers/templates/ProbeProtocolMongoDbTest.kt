package tmenier.fr.schedulers.templates

import org.bson.Document
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
import tmenier.fr.schedulers.services.DriverMongoDbHealthCheck
import tmenier.fr.schedulers.services.MongoDbHealthCheck
import java.time.Instant
import java.util.UUID

class ProbeProtocolMongoDbTest {
    private val encryptionService = EncryptionService("0123456789abcdef0123456789abcdef")
    private val healthCheck = DriverMongoDbHealthCheck(encryptionService)

    @Test
    fun `parses a replica set connection string`() {
        val connection =
            healthCheck.parseConnectionString(
                "mongodb://monitor:secret@db1.example.com:27017,db2.example.com/app?replicaSet=rs0&authSource=admin",
            )

        assertEquals(listOf("db1.example.com:27017", "db2.example.com"), connection.hosts)
        assertEquals("app", connection.database)
        assertEquals("monitor", connection.username)
        assertEquals("rs0", connection.requiredReplicaSetName)
    }

    @Test
    fun `parses an encrypted connection string without database`() {
        val connection =
            healthCheck.parseConnectionString(encryptionService.encrypt("mongodb://localhost"))

        assertEquals(listOf("localhost"), connection.hosts)
        assertNull(connection.database)
    }

    @Test
    fun `rejects a connection string with another scheme`() {
        assertThrows<IllegalArgumentException> {
            healthCheck.parseConnectionString("redis://localhost")
        }
    }

    @Test
    fun `parses relaxed JSON commands`() {
        assertEquals(Document("ping", 1), healthCheck.parseCommand("{ping: 1}"))
        assertEquals(Document("ping", 1), healthCheck.parseCommand("""{"ping": 1}"""))
        assertEquals(Document("ping", 1), healthCheck.parseCommand(" "))
    }

    @Test
    fun `rejects an invalid command`() {
        assertThrows<IllegalArgumentException> { healthCheck.parseCommand("ping") }
        assertThrows<IllegalArgumentException> { healthCheck.parseCommand("{}") }
    }

    @Test
    fun `accepts ok as integer or double`() {
        assertTrue(healthCheck.isOk(Document("ok", 1.0)))
        assertTrue(healthCheck.isOk(Document("ok", 1)))
        assertFalse(healthCheck.isOk(Document("ok", 0.0)))
        assertFalse(healthCheck.isOk(Document()))
    }

    @Test
    fun `returns success when MongoDB accepts the command`() {
        val executor = ProbeProtocolMongoDb(MongoDbHealthCheck { _, _ -> })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.SUCCESS, result.status)
        assertTrue(result.message.startsWith("MongoDB command successful"))
    }

    @Test
    fun `returns warning when a retry remains`() {
        val executor = ProbeProtocolMongoDb(MongoDbHealthCheck { _, _ -> error("Timed out while waiting for a server") })

        val result = executor.execute(probe(), content(), false)

        assertEquals(ProbeMonitorLogStatus.WARNING, result.status)
        assertTrue(result.message.contains("Timed out"))
    }

    @Test
    fun `returns failure on the last attempt`() {
        val executor = ProbeProtocolMongoDb(MongoDbHealthCheck { _, _ -> error("Authentication failed") })

        val result = executor.execute(probe(), content(), true)

        assertEquals(ProbeMonitorLogStatus.FAILURE, result.status)
        assertTrue(result.message.contains("Authentication failed"))
    }

    @Test
    fun `passes the probe timeout to the health check`() {
        var received: Int? = null
        val executor = ProbeProtocolMongoDb(MongoDbHealthCheck { _, timeoutSeconds -> received = timeoutSeconds })

        executor.execute(probe(timeout = 12), content(), true)

        assertEquals(12, received)
    }

    private fun content() =
        ProbeContent.MongoDb(
            connectionString = "mongodb://monitor:secret@mongo.example.com:27017/app",
            host = "mongo.example.com:27017/app",
        )

    private fun probe(timeout: Int = 5) =
        ProbeDTO(
            id = UUID.randomUUID(),
            name = "MongoDB test",
            interval = 60,
            timeout = timeout,
            retry = 1,
            intervalRetry = 1,
            enabled = true,
            protocol = ProbeProtocol.MONGODB,
            description = null,
            lastRun = null,
            status = ProbeMonitorLogStatus.SUCCESS,
            content = content(),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )
}
