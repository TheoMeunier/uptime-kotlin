package tmenier.fr.twofactor.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.exceptions.twofactor.InvalidTwoFactorCodeException
import tmenier.fr.common.exceptions.twofactor.TwoFactorAlreadyEnabledException
import tmenier.fr.common.exceptions.twofactor.TwoFactorSetupRequiredException
import tmenier.fr.common.utils.toUuidOrNull
import tmenier.fr.databases.repositories.RefreshTokenRepository
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.twofactor.dtos.requests.EnableTwoFactorRequest
import tmenier.fr.twofactor.dtos.responses.RecoveryCodesResponse
import tmenier.fr.twofactor.services.TwoFactorVerifier
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class EnableTwoFactorAction(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val verifier: TwoFactorVerifier,
) {
    fun execute(
        userId: UUID,
        payload: EnableTwoFactorRequest,
        now: Instant = Instant.now(),
    ): RecoveryCodesResponse {
        val user = userRepository.findById(userId)
        if (user.totpEnabledAt != null) throw TwoFactorAlreadyEnabledException()
        if (user.totpSecret == null) throw TwoFactorSetupRequiredException()

        val step = verifier.matchingStep(user, payload.code, now) ?: throw InvalidTwoFactorCodeException()

        userRepository.enableTwoFactor(user, step, now)
        val codes = verifier.regenerateRecoveryCodes(user)
        refreshTokenRepository.revokeAllForUserExcept(userId, payload.currentSessionId.toUuidOrNull())

        return RecoveryCodesResponse(codes)
    }
}
