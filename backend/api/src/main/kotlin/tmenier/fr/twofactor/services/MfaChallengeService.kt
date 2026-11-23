package tmenier.fr.twofactor.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.exceptions.twofactor.MfaChallengeInvalidException
import tmenier.fr.common.exceptions.twofactor.TooManyTwoFactorAttemptsException
import tmenier.fr.common.security.SecureRandomService
import tmenier.fr.common.utils.Sha256
import tmenier.fr.databases.entities.MfaChallengeEntity
import tmenier.fr.databases.repositories.MfaChallengeRepository
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class MfaChallengeService(
    private val repository: MfaChallengeRepository,
    private val randomService: SecureRandomService,
) {
    fun issue(
        userId: UUID,
        now: Instant = Instant.now(),
    ): String {
        repository.purgeCreatedBefore(now.minus(RETENTION))

        val token = randomService.urlSafeToken()
        repository.store(userId, Sha256.hex(token), now.plus(LIFETIME), now)

        return token
    }

    fun findUsable(
        token: String,
        now: Instant = Instant.now(),
    ): MfaChallengeEntity {
        val challenge = repository.findByTokenHashForUpdate(Sha256.hex(token)) ?: throw MfaChallengeInvalidException()

        val usable =
            challenge.consumedAt == null &&
                challenge.expiresAt > now &&
                challenge.attempts < MAX_ATTEMPTS_PER_CHALLENGE
        if (!usable) throw MfaChallengeInvalidException()

        return challenge
    }

    fun ensureNotLocked(
        userId: UUID,
        now: Instant = Instant.now(),
    ) {
        if (repository.failedAttemptsSince(userId, now.minus(ATTEMPTS_WINDOW)) >= MAX_ATTEMPTS_PER_USER) {
            throw TooManyTwoFactorAttemptsException()
        }
    }

    fun recordFailure(challenge: MfaChallengeEntity) = repository.recordFailedAttempt(challenge)

    fun consume(
        challenge: MfaChallengeEntity,
        now: Instant = Instant.now(),
    ) = repository.markConsumed(challenge, now)

    companion object {
        val LIFETIME: Duration = Duration.ofMinutes(5)
        val ATTEMPTS_WINDOW: Duration = Duration.ofMinutes(15)
        const val MAX_ATTEMPTS_PER_CHALLENGE = 5
        const val MAX_ATTEMPTS_PER_USER = 10
        private val RETENTION: Duration = Duration.ofHours(1)
    }
}
