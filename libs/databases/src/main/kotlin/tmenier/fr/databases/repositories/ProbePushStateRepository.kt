package tmenier.fr.databases.repositories

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import jakarta.transaction.Transactional
import tmenier.fr.databases.entities.ProbePushStateEntity
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID

@ApplicationScoped
class ProbePushStateRepository(
    private val em: EntityManager,
) : PanacheRepositoryBase<ProbePushStateEntity, UUID> {
    private val random = SecureRandom()

    @Transactional
    fun findByProbeId(probeId: UUID): ProbePushStateEntity? = findById(probeId)

    fun findByTokenForUpdate(token: String): ProbePushStateEntity? =
        find("token = ?1", token)
            .withLock(LockModeType.PESSIMISTIC_WRITE)
            .firstResult()

    @Transactional
    fun ensureFor(
        probeId: UUID,
        now: Instant = Instant.now(),
    ): ProbePushStateEntity {
        findById(probeId)?.let { return it }

        em.flush()

        return ProbePushStateEntity()
            .apply {
                this.probeId = probeId
                token = newToken()
                armedAt = now
            }.also { em.persist(it) }
    }

    @Transactional
    fun rearm(
        probeId: UUID,
        now: Instant = Instant.now(),
    ) {
        findById(probeId)?.armedAt = now
    }

    @Transactional
    fun regenerateToken(probeId: UUID): String? = findById(probeId)?.apply { token = newToken() }?.token

    @Transactional
    fun deleteFor(probeId: UUID) {
        deleteById(probeId)
    }

    private fun newToken(): String {
        val bytes = ByteArray(TOKEN_BYTES).also(random::nextBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    companion object {
        private const val TOKEN_BYTES = 32
        val TOKEN_FORMAT = Regex("^[A-Za-z0-9_-]{16,64}$")
    }
}
