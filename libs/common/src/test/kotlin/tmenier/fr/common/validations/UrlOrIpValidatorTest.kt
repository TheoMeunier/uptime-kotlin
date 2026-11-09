package tmenier.fr.common.validations

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource

class UrlOrIpValidatorTest {
    private val validator = UrlOrIpValidator()

    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://example.com",
            "https://example.com/health?full=1",
            "http://example.com",
            "http://intranet.local:8080/status",
            "http://192.168.1.10/health",
            "HTTP://example.com",
            "192.168.1.10",
        ],
    )
    fun `accepts http and https URLs and IPv4 addresses`(value: String) {
        assertTrue(validator.isValid(value, null), value)
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
        strings = [
            "ftp://example.com",
            "ws://example.com",
            "http://",
            "http:///path",
            "not a url",
        ],
    )
    fun `rejects other schemes and malformed values`(value: String?) {
        assertFalse(validator.isValid(value, null), value)
    }
}
