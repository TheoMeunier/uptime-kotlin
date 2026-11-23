package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.dtos.requests.LoginTotpRequest
import tmenier.fr.auth.dtos.responses.LoginResponse
import tmenier.fr.auth.services.SessionIssuer
import tmenier.fr.common.exceptions.twofactor.InvalidTwoFactorCodeException
import tmenier.fr.common.exceptions.twofactor.MfaChallengeInvalidException
import tmenier.fr.databases.mappers.UserMapper
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.twofactor.services.MfaChallengeService
import tmenier.fr.twofactor.services.TwoFactorVerifier
import java.time.Instant

@ApplicationScoped
class LoginTotpAction(
    private val challengeService: MfaChallengeService,
    private val userRepository: UserRepository,
    private val verifier: TwoFactorVerifier,
    private val sessionIssuer: SessionIssuer,
) {
    fun execute(
        payload: LoginTotpRequest,
        now: Instant = Instant.now(),
    ): LoginResponse {
        val challenge = challengeService.findUsable(payload.challengeToken, now)

        val user = userRepository.findByIdOrNull(challenge.userId)
        if (user == null || user.totpEnabledAt == null) throw MfaChallengeInvalidException()

        challengeService.ensureNotLocked(user.id, now)

        if (!verifier.accept(user, payload.code, allowRecoveryCode = true, now = now)) {
            challengeService.recordFailure(challenge)
            throw InvalidTwoFactorCodeException.atSignIn()
        }

        challengeService.consume(challenge, now)

        return sessionIssuer.open(UserMapper.fromEntity(user))
    }
}
