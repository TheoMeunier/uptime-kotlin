package tmenier.fr.common.security

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.Sha256

@ApplicationScoped
class RecoveryCodeService(
    private val randomService: SecureRandomService,
) {
    fun generate(count: Int = DEFAULT_COUNT): List<String> =
        generateSequence { randomService.groupedString(ALPHABET, GROUPS, GROUP_SIZE) }
            .distinct()
            .take(count)
            .toList()

    fun normalize(code: String): String = code.lowercase().filter(Char::isLetterOrDigit)

    fun isWellFormed(code: String): Boolean = NORMALIZED_PATTERN.matches(normalize(code))

    fun hash(code: String): String = Sha256.hex(normalize(code))

    companion object {
        const val DEFAULT_COUNT = 10
        const val ALPHABET = "abcdefghjkmnpqrstuvwxyz23456789"
        const val GROUPS = 2
        const val GROUP_SIZE = 5
        private val NORMALIZED_PATTERN = Regex("^[$ALPHABET]{${GROUPS * GROUP_SIZE}}$")
    }
}
