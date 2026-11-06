package tmenier.fr.common.enums.probes

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PushSignalTest {
    @Test
    fun `a bare call means the job succeeded`() {
        assertEquals(PushSignal.UP, PushSignal.parse(null))
        assertEquals(PushSignal.UP, PushSignal.parse(""))
        assertEquals(PushSignal.UP, PushSignal.parse("OK"))
    }

    @Test
    fun `failure aliases are accepted case-insensitively`() {
        listOf("down", "DOWN", "fail", "failure", " error ").forEach {
            assertEquals(PushSignal.DOWN, PushSignal.parse(it), it)
        }
    }

    @Test
    fun `an unknown status is rejected rather than guessed`() {
        assertNull(PushSignal.parse("maybe"))
    }
}
