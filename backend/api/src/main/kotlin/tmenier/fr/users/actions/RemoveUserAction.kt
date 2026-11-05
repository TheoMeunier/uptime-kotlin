package tmenier.fr.users.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.databases.repositories.UserRepository
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
        if (userId == currentUserId) {
            throw ApiException(OWN_ACCOUNT, "You cannot remove yourself.", Response.Status.CONFLICT)
        }

        val admins = userRepository.lockAdmins()
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User not found")

        if (user.role == UserRole.ADMIN && admins.size <= 1) {
            throw ApiException(LAST_ADMIN, "The instance needs at least one admin.", Response.Status.CONFLICT)
        }

        userRepository.delete(userId)
    }

    companion object {
        const val OWN_ACCOUNT = "OWN_ACCOUNT"
        const val LAST_ADMIN = "LAST_ADMIN"
    }
}
