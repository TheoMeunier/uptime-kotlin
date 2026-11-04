package tmenier.fr.common.enums.monitors

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProbeProtocolOrdinalTest {
    @Test
    fun `keeps the persisted ordinals stable`() {
        assertEquals(
            listOf(
                "HTTP",
                "TCP",
                "PING",
                "DNS",
                "POSTGRESQL",
                "SQLSERVER",
                "MYSQL",
                "REDIS",
                "SMTP",
                "KAFKA",
                "RABBITMQ",
                "MONGODB",
                "ORACLE",
                "WEBSOCKET",
            ),
            ProbeProtocol.entries.map { it.name },
        )
    }
}
