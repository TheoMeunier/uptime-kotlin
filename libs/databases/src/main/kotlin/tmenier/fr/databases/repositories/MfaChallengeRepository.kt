package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.LockModeType
import tmenier.fr.databases.entities.MfaChallengeEntity
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class MfaChallengeRepository : PanacheRepositoryBase<MfaChallengeEntity, UUID> {
    fun store(
        userId: UUID,
        tokenHash: String,
        expiresAt: Instant,
        now: Instant = Instant.now(),
    ) {
        MfaChallengeEntity()
            .apply {
                id = UUID.randomUUID()
                this.userId = userId
                this.tokenHash = tokenHash
                this.expiresAt = expiresAt
                createdAt = now
            }.persist()
    }

    fun findByTokenHashForUpdate(tokenHash: String): MfaChallengeEntity? =
        find("tokenHash = ?1", tokenHash)
            .withLock(LockModeType.PESSIMISTIC_WRITE)
            .firstResult()

    fun recordFailedAttempt(challenge: MfaChallengeEntity) {
        challenge.attempts += 1
        persist(challenge)
    }

    fun markConsumed(
        challenge: MfaChallengeEntity,
        at: Instant,
    ) {
        challenge.consumedAt = at
        persist(challenge)
    }

    fun failedAttemptsSince(
        userId: UUID,
        since: Instant,
    ): Long =
        getEntityManager()
            .createQuery(
                "SELECT COALESCE(SUM(c.attempts), 0) FROM MfaChallengeEntity c WHERE c.userId = :userId AND c.createdAt > :since",
                Number::class.java,
            ).setParameter("userId", userId)
            .setParameter("since", since)
            .singleResult
            .toLong()

    fun purgeCreatedBefore(before: Instant): Long = delete("createdAt < ?1", before)

    fun deleteAllForUser(userId: UUID): Long = delete("userId = ?1", userId)
}
