package tmenier.fr.setup.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.repositories.AdvisoryLockRepository
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.setup.dto.CreateFirstUserRequest
import java.util.Locale
import java.util.UUID

@ApplicationScoped
class CreateFirstUserAction(
    private val bcryptService: BcryptService,
    private val userRepository: UserRepository,
    private val advisoryLockRepository: AdvisoryLockRepository,
) {
    @Transactional
    fun execute(payload: CreateFirstUserRequest): Boolean {
        if (payload.password != payload.passwordConfirmation) return false

        if (!advisoryLockRepository.tryLock(AdvisoryLockRepository.FIRST_USER_SETUP)) return false

        if (userRepository.countAll() > 0) return false

        val userDto =
            UserDto(
                id = UUID.randomUUID(),
                name = payload.name.trim(),
                email = payload.email.trim().lowercase(Locale.ROOT),
                password = bcryptService.hashPassword(payload.password),
            )

        userRepository.store(userDto)

        return true
    }
}
