package tmenier.fr.common.enums.monitors

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.dtos.ProbeContent

class ProbeProtocolContentTest {
    @Test
    fun `every probe content belongs to exactly one protocol`() {
        val contentClasses = ProbeContent::class.sealedSubclasses.map { it.java }.toSet()
        val protocolClasses = ProbeProtocol.entries.map { it.contentClass }

        assertEquals(protocolClasses.size, protocolClasses.toSet().size, "two protocols share a content class")
        assertEquals(contentClasses, protocolClasses.toSet())
    }

    @Test
    fun `resolves the protocol of a content`() {
        assertEquals(ProbeProtocol.TCP, ProbeProtocol.of(ProbeContent.Tcp(url = "localhost", tcpPort = 1)))
        assertEquals(ProbeProtocol.PUSH, ProbeProtocol.of(ProbeContent.Push(periodSeconds = 60, graceSeconds = 0)))
    }
}
