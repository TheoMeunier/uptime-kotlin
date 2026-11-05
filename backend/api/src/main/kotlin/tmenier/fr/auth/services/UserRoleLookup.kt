package tmenier.fr.auth.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.repositories.UserRepository
import java.util.UUID

@ApplicationScoped
class UserRoleLookup(
    private val userRepository: UserRepository,
) {
    @ActivateRequestContext
    fun roleOf(userId: UUID): UserRole? = userRepository.roleOf(userId)
}
