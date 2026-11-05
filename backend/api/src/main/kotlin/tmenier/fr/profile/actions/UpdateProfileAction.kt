package tmenier.fr.profile.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.databases.mappers.UserMapper
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.profile.dtos.requests.UpdateProfileRequest
import tmenier.fr.users.actions.StoreUserAction.Companion.EMAIL_ALREADY_USED
import java.util.UUID

@ApplicationScoped
class UpdateProfileAction(
    private val jwt: JsonWebToken,
    private val userRepository: UserRepository,
) {
    fun execute(payload: UpdateProfileRequest) {
        val userId = UUID.fromString(jwt.name)
        val userDto = UserMapper.fromEntity(userRepository.findById(userId))

        if (userRepository.existsByEmail(payload.email.trim(), excludingId = userId)) {
            throw ApiException(EMAIL_ALREADY_USED, "An account already uses this email address.", Response.Status.CONFLICT)
        }

        val user =
            userDto.copy(
                name = payload.name,
                email = payload.email,
            )

        userRepository.update(user)
    }
}
