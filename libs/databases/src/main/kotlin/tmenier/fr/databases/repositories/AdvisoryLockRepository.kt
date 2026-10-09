package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager

@ApplicationScoped
class AdvisoryLockRepository(
    private val em: EntityManager,
) {
    fun tryLock(key: Long): Boolean {
        val acquired =
            em
                .createNativeQuery("SELECT pg_try_advisory_xact_lock(:key)")
                .setParameter("key", key)
                .singleResult

        return acquired as? Boolean ?: false
    }

    companion object {
        const val MAINTENANCE_MATERIALISER: Long = 8_110_004
        const val LOG_RETENTION: Long = 8_110_005
        const val SECRET_RE_ENCRYPTION: Long = 8_110_006
    }
}
