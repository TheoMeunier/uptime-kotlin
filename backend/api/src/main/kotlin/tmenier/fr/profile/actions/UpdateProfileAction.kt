package tmenier.fr.profile.actions

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.databases.mappers.UserMapper
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.profile.dtos.requests.UpdateProfileRequest
import java.util.UUID

@ApplicationScoped
class UpdateProfileAction(
    private val jwt: JsonWebToken,
    private val userRepository: UserRepository
) {
    fun execute(payload: UpdateProfileRequest) {
        val userId = UUID.fromString(jwt.name)
        val userDto = UserMapper.fromEntity(userRepository.findById(userId))

        val user = userDto.copy(
            name = payload.name,
            email = payload.email,
        )

        userRepository.update(user)
    }
}
