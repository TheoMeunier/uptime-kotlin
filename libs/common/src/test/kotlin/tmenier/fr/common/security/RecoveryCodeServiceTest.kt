package tmenier.fr.common.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RecoveryCodeServiceTest {
    private val service = RecoveryCodeService(SecureRandomService())

    @Test
    fun `ten distinct codes without ambiguous characters`() {
        val codes = service.generate()

        assertEquals(RecoveryCodeService.DEFAULT_COUNT, codes.size)
        assertEquals(codes.size, codes.toSet().size)
        codes.forEach { code ->
            assertTrue(Regex("^[a-z2-9]{5}-[a-z2-9]{5}$").matches(code), code)
            assertFalse(code.any { it in "01ilo" }, code)
            assertTrue(service.isWellFormed(code), code)
        }
    }

    @Test
    fun `case, spaces and dashes do not matter`() {
        assertEquals(service.hash("abcde-fghjk"), service.hash(" ABCDE FGHJK "))
        assertEquals(service.hash("abcde-fghjk"), service.hash("abcdefghjk"))
    }

    @Test
    fun `a TOTP code is never mistaken for a recovery code`() {
        assertFalse(service.isWellFormed("123456"))
    }

    @Test
    fun `the stored value is a SHA-256 hash, not the code`() {
        val hash = service.hash("abcde-fghjk")

        assertEquals(64, hash.length)
        assertFalse(hash.contains("abcde"))
    }
}
