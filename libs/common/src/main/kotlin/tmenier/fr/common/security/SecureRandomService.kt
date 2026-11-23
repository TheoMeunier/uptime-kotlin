package tmenier.fr.common.security

import jakarta.enterprise.context.ApplicationScoped
import java.security.SecureRandom
import java.util.Base64

@ApplicationScoped
class SecureRandomService {
    private val random = SecureRandom()

    fun bytes(size: Int): ByteArray = ByteArray(size).also(random::nextBytes)

    fun urlSafeToken(byteCount: Int = DEFAULT_TOKEN_BYTES): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes(byteCount))

    fun string(
        alphabet: String,
        length: Int,
    ): String = (0 until length).map { alphabet[random.nextInt(alphabet.length)] }.joinToString("")

    fun groupedString(
        alphabet: String,
        groups: Int,
        groupSize: Int,
        separator: String = "-",
    ): String = (0 until groups).joinToString(separator) { string(alphabet, groupSize) }

    companion object {
        const val DEFAULT_TOKEN_BYTES = 32
    }
}
