package tmenier.fr.common.totp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.security.SecureRandomService
import tmenier.fr.common.utils.Base32
import java.time.Instant

class TotpServiceTest {
    private val service = TotpService(SecureRandomService())

    @Test
    fun `codes match the RFC 6238 test vectors`() {
        mapOf(
            59L to "94287082",
            1_111_111_109L to "07081804",
            1_111_111_111L to "14050471",
            1_234_567_890L to "89005924",
            2_000_000_000L to "69279037",
            20_000_000_000L to "65353130",
        ).forEach { (epochSeconds, expected) ->
            val step = service.stepAt(Instant.ofEpochSecond(epochSeconds))
            assertEquals(expected, service.codeAt(RFC_SECRET, step, digits = 8), "T = $epochSeconds")
        }
    }

    @Test
    fun `six-digit codes are the last six digits of the RFC vectors`() {
        assertEquals("287082", service.codeAt(RFC_SECRET, service.stepAt(Instant.ofEpochSecond(59))))
    }

    @Test
    fun `one step of clock drift is accepted either way, two are not`() {
        val now = Instant.ofEpochSecond(1_234_567_890)
        val step = service.stepAt(now)

        assertEquals(step - 1, service.matchingStep(RFC_BASE32, codeAt(step - 1), now, null))
        assertEquals(step, service.matchingStep(RFC_BASE32, codeAt(step), now, null))
        assertEquals(step + 1, service.matchingStep(RFC_BASE32, codeAt(step + 1), now, null))
        assertNull(service.matchingStep(RFC_BASE32, codeAt(step - 2), now, null))
        assertNull(service.matchingStep(RFC_BASE32, codeAt(step + 2), now, null))
    }

    @Test
    fun `an accepted code cannot be replayed`() {
        val now = Instant.ofEpochSecond(1_234_567_890)
        val step = service.stepAt(now)

        assertNull(service.matchingStep(RFC_BASE32, codeAt(step), now, lastAcceptedStep = step))
        assertNull(service.matchingStep(RFC_BASE32, codeAt(step - 1), now, lastAcceptedStep = step))
        assertEquals(step + 1, service.matchingStep(RFC_BASE32, codeAt(step + 1), now, lastAcceptedStep = step))
    }

    @Test
    fun `anything but six digits is refused`() {
        val now = Instant.ofEpochSecond(59)

        listOf("", "28708", "2870820", "28708a", "94287082").forEach { code ->
            assertNull(service.matchingStep(RFC_BASE32, code, now, null), "accepted: '$code'")
        }
    }

    @Test
    fun `a generated secret is 160 bits of base32`() {
        val secret = service.generateSecret()

        assertEquals(32, secret.length)
        assertEquals(TotpService.SECRET_BYTES, Base32.decode(secret).size)
    }

    @Test
    fun `the provisioning uri carries what Bitwarden and authenticator apps expect`() {
        assertEquals(
            "otpauth://totp/Uptime%20Kotlin:user%40example.test?secret=JBSWY3DPEHPK3PXP&issuer=Uptime%20Kotlin" +
                "&algorithm=SHA1&digits=6&period=30",
            service.provisioningUri("Uptime Kotlin", "user@example.test", "JBSWY3DPEHPK3PXP"),
        )
    }

    @Test
    fun `codes are tolerated with spaces around and inside`() {
        val now = Instant.ofEpochSecond(59)

        assertTrue(service.matchingStep(RFC_BASE32, " 287 082 ", now, null) != null)
    }

    private fun codeAt(step: Long) = service.codeAt(RFC_SECRET, step)

    private companion object {
        val RFC_SECRET = "12345678901234567890".toByteArray(Charsets.US_ASCII)
        val RFC_BASE32 = Base32.encode(RFC_SECRET)
    }
}
