package tmenier.fr.common.bcrypt

import at.favre.lib.crypto.bcrypt.BCrypt
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class BcryptService {
    fun hashPassword(password: String): String = BCrypt.withDefaults().hashToString(12, password.toCharArray())

    fun verifyPassword(
        password: String,
        hash: String?,
    ): Boolean {
        val result = BCrypt.verifyer().verify(password.toCharArray(), hash)
        return result.verified
    }
}
