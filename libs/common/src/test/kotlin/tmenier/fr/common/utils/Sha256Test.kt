package tmenier.fr.common.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Sha256Test {
    @Test
    fun `sha256 is the lowercase hex digest`() {
        assertEquals("2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae", Sha256.hex("foo"))
    }
}
