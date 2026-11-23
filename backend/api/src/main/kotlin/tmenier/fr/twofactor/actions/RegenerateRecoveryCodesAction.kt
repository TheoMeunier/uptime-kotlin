package tmenier.fr.twofactor.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.exceptions.twofactor.InvalidTwoFactorCodeException
import tmenier.fr.common.exceptions.twofactor.TwoFactorNotEnabledException
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.twofactor.dtos.requests.RegenerateRecoveryCodesRequest
import tmenier.fr.twofactor.dtos.responses.RecoveryCodesResponse
import tmenier.fr.twofactor.services.TwoFactorVerifier
import java.util.UUID

@ApplicationScoped
class RegenerateRecoveryCodesAction(
    private val userRepository: UserRepository,
    private val verifier: TwoFactorVerifier,
) {
    fun execute(
        userId: UUID,
        payload: RegenerateRecoveryCodesRequest,
    ): RecoveryCodesResponse {
        val user = userRepository.findById(userId)
        if (user.totpEnabledAt == null) throw TwoFactorNotEnabledException()

        if (!verifier.accept(user, payload.code, allowRecoveryCode = false)) throw InvalidTwoFactorCodeException()

        return RecoveryCodesResponse(verifier.regenerateRecoveryCodes(user))
    }
}
