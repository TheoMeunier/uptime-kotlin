package tmenier.fr.users.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.security.SecureRandomService

@ApplicationScoped
class TemporaryPasswordGenerator(
    private val randomService: SecureRandomService,
) {
    fun generate(): String = randomService.groupedString(ALPHABET, GROUPS, GROUP_SIZE)

    companion object {
        const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
        const val GROUPS = 4
        const val GROUP_SIZE = 4
    }
}
