package tmenier.fr.profile.actions

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.auth.entities.UserEntity
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.profile.dtos.requests.UpdateProfileRequest
import java.util.*

@ApplicationScoped
class UpdateProfileAction(
    private val jwt: JsonWebToken,
) {
    fun execute(payload: UpdateProfileRequest) {
        val userId = UUID.fromString(jwt.name)
        val user = UserEntity.findById(userId) ?: throw NotFoundException("User not found")

        user.email = payload.email
        user.name = payload.name
        user.persist()
    }
}
