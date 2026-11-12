package tmenier.fr.users.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.databases.repositories.RefreshTokenRepository
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.users.services.TemporaryPasswordGenerator
import java.util.UUID

@ApplicationScoped
class ResetUserPasswordAction(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val bcryptService: BcryptService,
    private val passwordGenerator: TemporaryPasswordGenerator,
) {
    @Transactional
    fun execute(
        currentUserId: UUID,
        userId: UUID,
    ): String {
        if (userId == currentUserId) {
            throw ApiException(OWN_PASSWORD, "Change your own password from your profile.", Response.Status.CONFLICT)
        }

        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User not found")
        val password = passwordGenerator.generate()

        userRepository.setTemporaryPassword(user, bcryptService.hashPassword(password))
        refreshTokenRepository.revokeAllForUser(userId)

        return password
    }

    companion object {
        const val OWN_PASSWORD = "OWN_PASSWORD"
    }
}
