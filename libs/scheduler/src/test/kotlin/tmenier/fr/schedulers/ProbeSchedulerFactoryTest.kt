package tmenier.fr.schedulers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO

class ProbeSchedulerFactoryTest {
    @Test
    fun `indexes one handler per protocol`() {
        val handlers = ProbeProtocol.entries.map(::FakeHandler)

        val index = ProbeSchedulerFactory.index(handlers)

        assertEquals(ProbeProtocol.entries.toSet(), index.keys)
        assertSame(handlers.first { it.protocol == ProbeProtocol.TCP }, index[ProbeProtocol.TCP])
    }

    @Test
    fun `refuses a protocol without handler`() {
        val handlers = ProbeProtocol.entries.filter { it != ProbeProtocol.PUSH }.map(::FakeHandler)

        val error = assertThrows<IllegalStateException> { ProbeSchedulerFactory.index(handlers) }

        assertTrue(error.message!!.contains("PUSH"))
    }

    @Test
    fun `refuses two handlers for the same protocol`() {
        val handlers = ProbeProtocol.entries.map(::FakeHandler) + FakeHandler(ProbeProtocol.HTTP)

        val error = assertThrows<IllegalStateException> { ProbeSchedulerFactory.index(handlers) }

        assertTrue(error.message!!.contains("HTTP"))
    }

    private class FakeHandler(
        override val protocol: ProbeProtocol,
    ) : ProbeSchedulerInterface {
        override fun check(
            probe: ProbeDTO,
            isLastAttempt: Boolean,
        ): ProbeResult = error("not used")
    }
}
