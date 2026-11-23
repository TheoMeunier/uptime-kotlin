package tmenier.fr.common.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SecureRandomServiceTest {
    private val service = SecureRandomService()

    @Test
    fun `a url-safe token of 32 bytes is 43 characters without padding`() {
        val token = service.urlSafeToken()

        assertEquals(43, token.length)
        assertTrue(Regex("^[A-Za-z0-9_-]+$").matches(token))
        assertNotEquals(token, service.urlSafeToken())
    }

    @Test
    fun `grouped strings only use the given alphabet`() {
        val value = service.groupedString("ab", groups = 3, groupSize = 4, separator = ".")

        assertTrue(Regex("^[ab]{4}\\.[ab]{4}\\.[ab]{4}$").matches(value), value)
    }
}
