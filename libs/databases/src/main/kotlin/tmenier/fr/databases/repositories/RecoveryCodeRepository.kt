package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.entities.RecoveryCodeEntity
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class RecoveryCodeRepository : PanacheRepositoryBase<RecoveryCodeEntity, UUID> {
    fun replaceAll(
        userId: UUID,
        codeHashes: List<String>,
    ) {
        deleteAllForUser(userId)

        val now = Instant.now()
        codeHashes.forEach { hash ->
            RecoveryCodeEntity()
                .apply {
                    id = UUID.randomUUID()
                    this.userId = userId
                    codeHash = hash
                    createdAt = now
                }.persist()
        }
    }

    fun consume(
        userId: UUID,
        codeHash: String,
        at: Instant = Instant.now(),
    ): Boolean = update("usedAt = ?1 where userId = ?2 and codeHash = ?3 and usedAt is null", at, userId, codeHash) > 0

    fun countRemaining(userId: UUID): Long = count("userId = ?1 and usedAt is null", userId)

    fun deleteAllForUser(userId: UUID): Long = delete("userId = ?1", userId)
}
