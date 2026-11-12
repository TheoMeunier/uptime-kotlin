package tmenier.fr.users.services

import jakarta.enterprise.context.ApplicationScoped
import java.security.SecureRandom

@ApplicationScoped
class TemporaryPasswordGenerator {
    private val random = SecureRandom()

    fun generate(): String =
        (0 until GROUPS).joinToString("-") {
            (0 until GROUP_SIZE).map { ALPHABET[random.nextInt(ALPHABET.length)] }.joinToString("")
        }

    companion object {
        const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
        const val GROUPS = 4
        const val GROUP_SIZE = 4
    }
}
