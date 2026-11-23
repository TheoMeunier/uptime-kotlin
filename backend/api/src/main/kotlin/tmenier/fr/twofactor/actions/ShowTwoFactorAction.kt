package tmenier.fr.twofactor.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.repositories.RecoveryCodeRepository
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.twofactor.dtos.responses.TwoFactorStatusResponse
import java.util.UUID

@ApplicationScoped
class ShowTwoFactorAction(
    private val userRepository: UserRepository,
    private val recoveryCodeRepository: RecoveryCodeRepository,
) {
    fun execute(userId: UUID): TwoFactorStatusResponse {
        val enabledAt = userRepository.findById(userId).totpEnabledAt

        return TwoFactorStatusResponse(
            enabled = enabledAt != null,
            enabledAt = enabledAt,
            recoveryCodesRemaining = if (enabledAt != null) recoveryCodeRepository.countRemaining(userId) else 0,
        )
    }
}
