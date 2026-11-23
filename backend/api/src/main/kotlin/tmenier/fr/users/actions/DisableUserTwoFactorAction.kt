package tmenier.fr.users.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.common.exceptions.twofactor.OwnTwoFactorException
import tmenier.fr.common.exceptions.twofactor.TwoFactorNotEnabledException
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.twofactor.services.TwoFactorVerifier
import java.util.UUID

@ApplicationScoped
class DisableUserTwoFactorAction(
    private val userRepository: UserRepository,
    private val verifier: TwoFactorVerifier,
) {
    @Transactional
    fun execute(
        currentUserId: UUID,
        userId: UUID,
    ) {
        if (userId == currentUserId) throw OwnTwoFactorException()

        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User not found")
        if (user.totpEnabledAt == null && user.totpSecret == null) throw TwoFactorNotEnabledException()

        verifier.disable(user)
    }
}
