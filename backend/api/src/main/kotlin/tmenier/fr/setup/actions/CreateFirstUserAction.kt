package tmenier.fr.setup.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.setup.dto.CreateFirstUserRequest
import java.util.UUID

@ApplicationScoped
class CreateFirstUserAction(
    private val bcryptService: BcryptService,
    private val userRepository: UserRepository
) {
    @Transactional
    fun execute(payload: CreateFirstUserRequest): Boolean {
        val totalUser = userRepository.countAll()

        if (totalUser.toInt() > 0) return false

        if (payload.password != payload.passwordConfirmation) return false

        val userDto = UserDto(
            id = UUID.randomUUID(),
            name = payload.name,
            email = payload.email,
            password = bcryptService.hashPassword(payload.password),
        )

        userRepository.store(userDto)

        return true
    }
}
