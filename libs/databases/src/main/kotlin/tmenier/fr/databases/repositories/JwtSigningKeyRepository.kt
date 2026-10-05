package tmenier.fr.databases.repositories

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional

data class StoredJwtSigningKey(
    val publicKey: String,
    val privateKey: String,
)

@ApplicationScoped
class JwtSigningKeyRepository(
    private val em: EntityManager,
) {
    @Transactional
    fun findOrCreate(generate: () -> StoredJwtSigningKey): StoredJwtSigningKey {
        find()?.let { return it }

        val candidate = generate()
        em
            .createNativeQuery(
                "INSERT INTO jwt_signing_keys (id, public_key, private_key) VALUES (:id, :publicKey, :privateKey) ON CONFLICT (id) DO NOTHING",
            ).setParameter("id", SINGLETON_ID)
            .setParameter("publicKey", candidate.publicKey)
            .setParameter("privateKey", candidate.privateKey)
            .executeUpdate()

        return checkNotNull(find()) { "jwt_signing_keys is still empty after inserting the generated key" }
    }

    private fun find(): StoredJwtSigningKey? {
        val row =
            em
                .createNativeQuery("SELECT public_key, private_key FROM jwt_signing_keys WHERE id = :id")
                .setParameter("id", SINGLETON_ID)
                .resultList
                .firstOrNull() as Array<*>? ?: return null

        return StoredJwtSigningKey(publicKey = row[0] as String, privateKey = row[1] as String)
    }

    private companion object {
        const val SINGLETON_ID: Short = 1
    }
}
