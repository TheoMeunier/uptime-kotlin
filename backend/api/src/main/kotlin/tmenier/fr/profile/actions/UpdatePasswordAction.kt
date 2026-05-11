package tmenier.fr.profile.actions

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.profile.dtos.requests.UpdatePasswordRequest
import java.util.UUID

@ApplicationScoped
class UpdatePasswordAction(
    private val jwt: JsonWebToken,
    private val passwordService: BcryptService,
    private val userRepository: UserRepository
) {
    fun execute(payload: UpdatePasswordRequest) {
        val userId = UUID.fromString(jwt.name)

        if (payload.password != payload.passwordConfirmation) {
            throw BadRequestException("Passwords do not match")
        }

        val password = passwordService.hashPassword(payload.password)
        userRepository.updatePassword(userId, password)
    }
}
