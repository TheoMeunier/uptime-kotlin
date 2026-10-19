package tmenier.fr.common.validations

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource

class HostnameOrIpValidatorTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "smtp.google.com",
            "smtp.gmail.com",
            "mail-relay.example.co.uk",
            "mailhog",
            "smtp.example.com.",
            "192.168.1.10",
            "::1",
            "[2001:db8::25]",
            "2001:db8::25",
            "https://smtp.google.com",
            "http://smtp.google.com",
            "  smtp.google.com  ",
        ],
    )
    fun `accepts hostnames, IP addresses and legacy URL-prefixed hosts`(value: String) {
        assertTrue(HostnameOrIpValidator.isValidHostnameOrIp(value), value)
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
        strings = [
            " ",
            "https://",
            "smtp google.com",
            "-smtp.google.com",
            "smtp-.google.com",
            "smtp..google.com",
            "smtp_relay.google.com",
            "smtp.google.com/path",
            "smtp.google.com:587",
            "https://smtp.google.com/",
            "999.1.1.1",
            "192.168.1",
            "2001:db8::zz",
        ],
    )
    fun `rejects values that are not a bare host`(value: String?) {
        assertFalse(HostnameOrIpValidator.isValidHostnameOrIp(value), value.toString())
    }
}
