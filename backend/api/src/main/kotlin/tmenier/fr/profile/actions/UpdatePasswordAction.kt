package tmenier.fr.profile.actions

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.databases.repositories.RefreshTokenRepository
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.profile.dtos.requests.UpdatePasswordRequest
import java.util.UUID

@ApplicationScoped
class UpdatePasswordAction(
    private val jwt: JsonWebToken,
    private val passwordService: BcryptService,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun execute(payload: UpdatePasswordRequest) {
        val userId = UUID.fromString(jwt.name)

        if (payload.password != payload.passwordConfirmation) {
            throw BadRequestException("Passwords do not match")
        }

        val user = userRepository.findById(userId)

        if (!passwordService.verifyPassword(payload.currentPassword, user.password)) {
            throw BadRequestException("Current password is incorrect")
        }

        if (payload.currentPassword == payload.password) {
            throw BadRequestException("New password must be different from the current password")
        }

        val password = passwordService.hashPassword(payload.password)
        userRepository.updatePassword(userId, password)

        refreshTokenRepository.revokeAllForUser(userId)
    }
}
