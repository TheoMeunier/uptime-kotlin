package tmenier.fr.common.encryption

import io.quarkus.arc.Unremovable
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.SecureRandom
import java.util.Base64
import java.util.Optional
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Startup
@Unremovable
@ApplicationScoped
class EncryptionService
    @Inject
    constructor(
        @ConfigProperty(name = "encryption.master-key")
        masterKey: String,
        @ConfigProperty(name = "encryption.legacy-master-key")
        legacyMasterKey: Optional<String>,
        @ConfigProperty(name = "encryption.previous-master-keys")
        previousMasterKeys: Optional<List<String>>,
    ) {
        constructor(masterKey: String) : this(masterKey, Optional.empty(), Optional.empty())

        constructor(masterKey: String, legacyMasterKey: Optional<String>) : this(masterKey, legacyMasterKey, Optional.empty())

        private val secretKey: SecretKey
        private val previousSecretKeys: List<SecretKey>
        private val legacySecretKey: SecretKey
        private val secureRandom = SecureRandom()

        init {
            secretKey = deriveKey(masterKey, "encryption.master-key (ENCRYPTION_MASTER_KEY)")
            previousSecretKeys =
                previousMasterKeys
                    .orElse(emptyList())
                    .map(String::trim)
                    .filter(String::isNotEmpty)
                    .filter { it != masterKey }
                    .map { deriveKey(it, "Each encryption.previous-master-keys (ENCRYPTION_PREVIOUS_MASTER_KEYS) entry") }

            val legacyKeyBytes = legacyMasterKey.orElse(masterKey).toByteArray(Charsets.UTF_8)
            legacySecretKey = SecretKeySpec(legacyKeyBytes.copyOf(AES_KEY_BYTES), AES)
        }

        fun encrypt(plainText: String): String {
            val iv = ByteArray(IV_BYTES).also(secureRandom::nextBytes)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_BITS, iv))

            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            return PREFIX_V2 + Base64.getEncoder().encodeToString(iv + encrypted)
        }

        fun decrypt(encryptedText: String): String =
            if (encryptedText.startsWith(PREFIX_V2)) {
                decryptV2(encryptedText.removePrefix(PREFIX_V2))
            } else {
                decryptWith(legacySecretKey, encryptedText.removePrefix(PREFIX_V1))
            }

        fun decryptIfEncrypted(value: String): String {
            if (hasEncryptionMarker(value)) return decrypt(value)
            if ("://" in value) return value

            return runCatching { decrypt(value) }.getOrDefault(value)
        }

        fun hasEncryptionMarker(value: String): Boolean = value.startsWith(PREFIX_V2) || value.startsWith(PREFIX_V1)

        fun isEncryptedWithCurrentKey(value: String): Boolean =
            value.startsWith(PREFIX_V2) &&
                runCatching { decryptWith(secretKey, value.removePrefix(PREFIX_V2)) }.isSuccess

        fun reEncrypt(value: String): String? {
            require(hasEncryptionMarker(value)) { "Not an encrypted value" }
            if (isEncryptedWithCurrentKey(value)) return null

            return encrypt(decrypt(value))
        }

        private fun decryptV2(payload: String): String {
            for (key in listOf(secretKey) + previousSecretKeys) {
                try {
                    return decryptWith(key, payload)
                } catch (ignored: AEADBadTagException) {
                    // AES-GCM authenticates the ciphertext: a wrong key always fails here, try the next one.
                }
            }
            throw AEADBadTagException(
                "Value encrypted with a key that is neither ENCRYPTION_MASTER_KEY nor one of ENCRYPTION_PREVIOUS_MASTER_KEYS",
            )
        }

        private fun deriveKey(
            masterKey: String,
            name: String,
        ): SecretKey {
            val masterKeyBytes = masterKey.toByteArray(Charsets.UTF_8)
            require(masterKeyBytes.size >= MIN_MASTER_KEY_BYTES) {
                "$name must be at least $MIN_MASTER_KEY_BYTES bytes, " +
                    "got ${masterKeyBytes.size}. Generate one with `openssl rand -base64 32`."
            }
            return SecretKeySpec(hkdfSha256(masterKeyBytes), AES)
        }

        private fun decryptWith(
            key: SecretKey,
            payload: String,
        ): String {
            val combined = Base64.getDecoder().decode(payload)
            val iv = combined.copyOfRange(0, IV_BYTES)
            val encrypted = combined.copyOfRange(IV_BYTES, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
            return String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }

        private fun hkdfSha256(inputKeyMaterial: ByteArray): ByteArray {
            val pseudoRandomKey = hmacSha256(HKDF_SALT, inputKeyMaterial)
            return hmacSha256(pseudoRandomKey, HKDF_INFO + 0x01.toByte())
        }

        private fun hmacSha256(
            key: ByteArray,
            data: ByteArray,
        ): ByteArray =
            Mac.getInstance(HMAC_SHA256).run {
                init(SecretKeySpec(key, HMAC_SHA256))
                doFinal(data)
            }

        private companion object {
            const val PREFIX_V1 = "enc:v1:"
            const val PREFIX_V2 = "enc:v2:"
            const val MIN_MASTER_KEY_BYTES = 32
            const val AES_KEY_BYTES = 32
            const val IV_BYTES = 12
            const val TAG_BITS = 128
            const val AES = "AES"
            const val TRANSFORMATION = "AES/GCM/NoPadding"
            const val HMAC_SHA256 = "HmacSHA256"
            val HKDF_SALT = "uptime-kotlin/encryption".toByteArray(Charsets.UTF_8)
            val HKDF_INFO = "aes-256-gcm/v2".toByteArray(Charsets.UTF_8)
        }
    }
