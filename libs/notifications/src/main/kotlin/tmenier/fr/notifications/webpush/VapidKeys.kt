package tmenier.fr.notifications.webpush

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.databases.repositories.StoredVapidKey
import tmenier.fr.databases.repositories.WebPushVapidKeyRepository
import java.security.KeyPair
import java.security.interfaces.ECPublicKey
import java.util.Base64

fun interface VapidKeySource {
    fun current(): KeyPair
}

@ApplicationScoped
class StoredVapidKeySource(
    private val repository: WebPushVapidKeyRepository,
    private val encryption: EncryptionService,
) : VapidKeySource {
    @Volatile
    private var cached: KeyPair? = null

    override fun current(): KeyPair = cached ?: synchronized(this) { cached ?: load().also { cached = it } }

    fun publicKey(): String = WebPushCrypto.base64Url(WebPushCrypto.rawPublicKey(current().public as ECPublicKey))

    private fun load(): KeyPair {
        val stored = repository.findOrCreate(::generate)
        val privateKey =
            runCatching { encryption.decrypt(stored.privateKey) }.getOrElse {
                throw IllegalStateException(
                    "Unable to decrypt the VAPID key stored in web_push_vapid_keys: it was encrypted with neither " +
                        "ENCRYPTION_MASTER_KEY nor ENCRYPTION_PREVIOUS_MASTER_KEYS.",
                    it,
                )
            }

        return KeyPair(
            WebPushCrypto.publicKeyFromRaw(WebPushCrypto.decodeBase64(stored.publicKey)),
            WebPushCrypto.privateKeyFromPkcs8(Base64.getDecoder().decode(privateKey)),
        )
    }

    private fun generate(): StoredVapidKey {
        val pair = WebPushCrypto.generateKeyPair()
        return StoredVapidKey(
            publicKey = WebPushCrypto.base64Url(WebPushCrypto.rawPublicKey(pair.public as ECPublicKey)),
            privateKey = encryption.encrypt(Base64.getEncoder().encodeToString(pair.private.encoded)),
        )
    }
}
