package tmenier.fr.common.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class UuidParsingTest {
    @Test
    fun `an invalid or missing uuid is null, not an exception`() {
        assertNull("not-a-uuid".toUuidOrNull())
        assertNull((null as String?).toUuidOrNull())
    }

    @Test
    fun `a valid uuid is parsed`() {
        val id = UUID.fromString("11111111-1111-1111-1111-111111111111")

        assertEquals(id, id.toString().toUuidOrNull())
    }
}
