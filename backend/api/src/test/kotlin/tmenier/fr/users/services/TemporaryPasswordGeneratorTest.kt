package tmenier.fr.users.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TemporaryPasswordGeneratorTest {
    private val generator = TemporaryPasswordGenerator()

    @Test
    fun `passwords are four groups of four unambiguous characters`() {
        repeat(200) {
            val password = generator.generate()

            assertTrue(Regex("^[A-Za-z0-9]{4}(-[A-Za-z0-9]{4}){3}$").matches(password), password)
            assertTrue(password.replace("-", "").all { it in TemporaryPasswordGenerator.ALPHABET }, password)
            assertTrue(password.none { it in "0O1lI" }, password)
        }
    }

    @Test
    fun `passwords fit the account password rules`() {
        val password = generator.generate()

        assertTrue(password.length in 8..72)
    }

    @Test
    fun `passwords do not repeat`() {
        assertEquals(500, (1..500).map { generator.generate() }.toSet().size)
    }
}
