package tmenier.fr.databases.repositories

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional

@RegisterForReflection
data class StoredVapidKey(
    val publicKey: String,
    val privateKey: String,
)

@ApplicationScoped
class WebPushVapidKeyRepository(
    private val em: EntityManager,
) {
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun findOrCreate(generate: () -> StoredVapidKey): StoredVapidKey {
        find()?.let { return it }

        val candidate = generate()
        em
            .createNativeQuery(
                "INSERT INTO web_push_vapid_keys (id, public_key, private_key) VALUES (:id, :publicKey, :privateKey) ON CONFLICT (id) DO NOTHING",
            ).setParameter("id", SINGLETON_ID)
            .setParameter("publicKey", candidate.publicKey)
            .setParameter("privateKey", candidate.privateKey)
            .executeUpdate()

        return checkNotNull(find()) { "web_push_vapid_keys is still empty after inserting the generated key" }
    }

    fun replacePrivateKey(
        previous: String,
        next: String,
    ): Boolean =
        em
            .createNativeQuery("UPDATE web_push_vapid_keys SET private_key = :next WHERE id = :id AND private_key = :previous")
            .setParameter("next", next)
            .setParameter("id", SINGLETON_ID)
            .setParameter("previous", previous)
            .executeUpdate() == 1

    fun find(): StoredVapidKey? {
        val row =
            em
                .createNativeQuery("SELECT public_key, private_key FROM web_push_vapid_keys WHERE id = :id")
                .setParameter("id", SINGLETON_ID)
                .resultList
                .firstOrNull() as Array<*>? ?: return null

        return StoredVapidKey(publicKey = row[0] as String, privateKey = row[1] as String)
    }

    private companion object {
        const val SINGLETON_ID: Short = 1
    }
}
