package tmenier.fr.common.enums.monitors

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** probes.protocol stores the ordinal: changing this list silently remaps existing probes. */
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
            ),
            ProbeProtocol.entries.map { it.name },
        )
    }
}
