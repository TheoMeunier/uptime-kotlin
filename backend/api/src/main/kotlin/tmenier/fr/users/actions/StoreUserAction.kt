package tmenier.fr.users.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.users.requests.StoreUserRequest
import java.util.Locale
import java.util.UUID

@ApplicationScoped
class StoreUserAction(
    private val userRepository: UserRepository,
    private val bcryptService: BcryptService,
) {
    @Transactional
    fun execute(payload: StoreUserRequest): UUID {
        val email = payload.email.trim().lowercase(Locale.ROOT)

        if (userRepository.existsByEmail(email)) {
            throw ApiException(EMAIL_ALREADY_USED, "An account already uses this email address.", Response.Status.CONFLICT)
        }

        val id = UUID.randomUUID()

        userRepository.store(
            UserDto(
                id = id,
                name = payload.name.trim(),
                email = email,
                password = bcryptService.hashPassword(payload.password),
                role = payload.role,
            ),
        )

        return id
    }

    companion object {
        const val EMAIL_ALREADY_USED = "EMAIL_ALREADY_USED"
    }
}
