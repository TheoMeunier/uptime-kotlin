package tmenier.fr.common.providers

import jakarta.ws.rs.WebApplicationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tmenier.fr.common.exceptions.core.ApiErrorResponse
import java.util.UUID

class UuidParamConverterTest {
    @Test
    fun `a well formed identifier is converted`() {
        val value = "11111111-1111-1111-1111-111111111111"

        assertEquals(UUID.fromString(value), UuidParamConverter.fromString(value))
    }

    @Test
    fun `a missing identifier stays null`() {
        assertNull(UuidParamConverter.fromString(null))
    }

    @Test
    fun `a malformed identifier is rejected with a 400`() {
        val exception = assertThrows<WebApplicationException> { UuidParamConverter.fromString("not-a-uuid") }

        assertEquals(400, exception.response.status)
        assertEquals("BAD_REQUEST", (exception.response.entity as ApiErrorResponse).error)
    }

    @Test
    fun `a truncated identifier is rejected instead of being padded`() {
        assertThrows<WebApplicationException> { UuidParamConverter.fromString("1-1-1-1-1") }
    }

    @Test
    fun `an empty identifier is rejected`() {
        assertThrows<WebApplicationException> { UuidParamConverter.fromString("") }
    }

    @Test
    fun `the provider only handles UUID parameters`() {
        val provider = UuidParamConverterProvider()

        assertEquals(UuidParamConverter, provider.getConverter(UUID::class.java, null, emptyArray()))
        assertNull(provider.getConverter(String::class.java, null, emptyArray()))
    }
}
