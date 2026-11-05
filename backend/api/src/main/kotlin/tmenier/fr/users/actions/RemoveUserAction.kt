package tmenier.fr.users.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.users.actions.UpdateUserRoleAction.Companion.lastAdmin
import tmenier.fr.users.actions.UpdateUserRoleAction.Companion.ownAccount
import java.util.UUID

@ApplicationScoped
class RemoveUserAction(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun execute(
        currentUserId: UUID,
        userId: UUID,
    ) {
        if (userId == currentUserId) throw ownAccount()

        val admins = userRepository.lockAdmins()
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User not found")

        if (user.role == UserRole.ADMIN && admins.size <= 1) throw lastAdmin()

        userRepository.delete(userId)
    }
}
