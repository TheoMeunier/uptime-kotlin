package tmenier.fr.setup.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.auth.entities.UserEntity
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.setup.dto.CreateFirstUserRequest
import java.util.UUID

@ApplicationScoped
class CreateFirstUserAction(
    private val bcryptService: BcryptService,
) {
    @Transactional
    fun execute(payload: CreateFirstUserRequest): Boolean {
        val totalUser = UserEntity.count()

        if (totalUser.toInt() > 0) return false

        if (payload.password != payload.passwordConfirmation) return false

        val hashedPassword = bcryptService.hashPassword(payload.password)

        val user =
            UserEntity().apply {
                id = UUID.randomUUID()
                name = payload.name
                email = payload.email
                password = hashedPassword
            }

        user.persist()

        return true
    }
}
