package tmenier.fr.profile.actions

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.auth.entities.UserEntity
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.profile.dtos.requests.UpdatePasswordRequest
import java.util.UUID

@ApplicationScoped
class UpdatePasswordAction(
    private val jwt: JsonWebToken,
    private val passwordService: BcryptService,
) {
    fun execute(payload: UpdatePasswordRequest) {
        val userId = UUID.fromString(jwt.name)
        val user = UserEntity.findById(userId) ?: throw NotFoundException("User not found")

        if (payload.password != payload.passwordConfirmation) {
            throw BadRequestException("Passwords do not match")
        }

        user.password = passwordService.hashPassword(payload.password)
        user.persist()
    }
}
