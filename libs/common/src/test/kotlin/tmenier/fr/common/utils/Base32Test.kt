package tmenier.fr.common.utils

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Base32Test {
    @Test
    fun `base32 follows RFC 4648 without padding`() {
        mapOf(
            "" to "",
            "f" to "MY",
            "fo" to "MZXQ",
            "foo" to "MZXW6",
            "foob" to "MZXW6YQ",
            "fooba" to "MZXW6YTB",
            "foobar" to "MZXW6YTBOI",
        ).forEach { (plain, encoded) ->
            assertEquals(encoded, Base32.encode(plain.toByteArray()), plain)
            assertArrayEquals(plain.toByteArray(), Base32.decode(encoded), encoded)
        }
    }

    @Test
    fun `decoding tolerates padding, spaces and lower case`() {
        assertArrayEquals("foobar".toByteArray(), Base32.decode("mzxw 6ytb oi======"))
    }
}
