package tmenier.fr.common.encryption

import io.quarkus.arc.Unremovable
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.SecureRandom
import java.util.Base64
import java.util.Optional
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
    ) {
        constructor(masterKey: String) : this(masterKey, Optional.empty())

        private val secretKey: SecretKey
        private val legacySecretKey: SecretKey
        private val secureRandom = SecureRandom()

        init {
            val masterKeyBytes = masterKey.toByteArray(Charsets.UTF_8)
            require(masterKeyBytes.size >= MIN_MASTER_KEY_BYTES) {
                "encryption.master-key (ENCRYPTION_MASTER_KEY) must be at least $MIN_MASTER_KEY_BYTES bytes, " +
                    "got ${masterKeyBytes.size}. Generate one with `openssl rand -base64 32`."
            }
            secretKey = SecretKeySpec(hkdfSha256(masterKeyBytes), AES)

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
                decryptWith(secretKey, encryptedText.removePrefix(PREFIX_V2))
            } else {
                decryptWith(legacySecretKey, encryptedText.removePrefix(PREFIX_V1))
            }

        fun decryptIfEncrypted(value: String): String {
            if (value.startsWith(PREFIX_V2) || value.startsWith(PREFIX_V1)) return decrypt(value)
            if ("://" in value) return value

            return runCatching { decrypt(value) }.getOrDefault(value)
        }

        fun isEncryptedWithCurrentKey(value: String): Boolean = value.startsWith(PREFIX_V2)

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
