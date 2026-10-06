package tmenier.fr.notifications

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonTextTest {
    @Test
    fun `escapes quotes, backslashes and line breaks`() {
        assertEquals("""say \"hi\"\nC:\\path\tend""", JsonText.escape("say \"hi\"\nC:\\path\tend"))
    }

    @Test
    fun `escapes other control characters as unicode`() {
        assertEquals("""a\u0001b""", JsonText.escape("a\u0001b"))
    }

    @Test
    fun `leaves accents and emoji untouched`() {
        assertEquals("Échec 🔴", JsonText.escape("Échec 🔴"))
    }
}
