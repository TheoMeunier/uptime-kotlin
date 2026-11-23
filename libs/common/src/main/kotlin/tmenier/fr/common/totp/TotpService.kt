package tmenier.fr.common.totp

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.security.SecureRandomService
import tmenier.fr.common.utils.Base32
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@ApplicationScoped
class TotpService(
    private val randomService: SecureRandomService,
) {
    fun generateSecret(): String = Base32.encode(randomService.bytes(SECRET_BYTES))

    fun stepAt(now: Instant): Long = Math.floorDiv(now.epochSecond, PERIOD_SECONDS)

    fun codeAt(
        secret: ByteArray,
        step: Long,
        digits: Int = DIGITS,
    ): String {
        require(digits in 1 until POWERS.size) { "Unsupported number of digits: $digits" }

        val mac = Mac.getInstance(HMAC)
        mac.init(SecretKeySpec(secret, HMAC))
        val hash = mac.doFinal(ByteBuffer.allocate(Long.SIZE_BYTES).putLong(step).array())

        val offset = hash.last().toInt() and 0x0f
        val binary =
            ((hash[offset].toInt() and 0x7f) shl 24) or
                ((hash[offset + 1].toInt() and 0xff) shl 16) or
                ((hash[offset + 2].toInt() and 0xff) shl 8) or
                (hash[offset + 3].toInt() and 0xff)

        return (binary % POWERS[digits]).toString().padStart(digits, '0')
    }

    fun isWellFormed(code: String): Boolean = CODE_PATTERN.matches(code)

    fun matchingStep(
        base32Secret: String,
        code: String,
        now: Instant,
        lastAcceptedStep: Long?,
    ): Long? {
        val candidate = code.trim().replace(" ", "")
        if (!isWellFormed(candidate)) return null

        val secret = Base32.decode(base32Secret)
        val current = stepAt(now)
        var match: Long? = null

        for (step in current - DRIFT_STEPS..current + DRIFT_STEPS) {
            val equal = MessageDigest.isEqual(codeAt(secret, step).toByteArray(), candidate.toByteArray())
            val fresh = lastAcceptedStep == null || step > lastAcceptedStep
            if (equal && fresh && match == null) match = step
        }

        return match
    }

    fun provisioningUri(
        issuer: String,
        account: String,
        base32Secret: String,
    ): String {
        val label = encode("$issuer:$account").replace("%3A", ":")

        return "otpauth://totp/$label" +
            "?secret=$base32Secret" +
            "&issuer=${encode(issuer)}" +
            "&algorithm=$ALGORITHM" +
            "&digits=$DIGITS" +
            "&period=$PERIOD_SECONDS"
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8).replace("+", "%20")

    companion object {
        const val DIGITS = 6
        const val PERIOD_SECONDS = 30L
        const val ALGORITHM = "SHA1"
        const val SECRET_BYTES = 20
        private const val DRIFT_STEPS = 1L
        private const val HMAC = "HmacSHA1"
        private val CODE_PATTERN = Regex("^\\d{$DIGITS}$")
        private val POWERS = intArrayOf(1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000)
    }
}
