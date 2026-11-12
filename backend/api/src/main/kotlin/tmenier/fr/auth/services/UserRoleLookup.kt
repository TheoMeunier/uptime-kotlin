package tmenier.fr.auth.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.repositories.UserRepository
import java.util.UUID

data class AccountAccess(
    val role: UserRole,
    val mustChangePassword: Boolean,
)

@ApplicationScoped
class UserRoleLookup(
    private val userRepository: UserRepository,
) {
    @ActivateRequestContext
    fun accessOf(userId: UUID): AccountAccess? = userRepository.findByIdOrNull(userId)?.let { AccountAccess(it.role, it.mustChangePassword) }
}
