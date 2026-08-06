package tmenier.fr.common.encryption

import io.quarkus.arc.Unremovable
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Unremovable
@ApplicationScoped
class EncryptionService(
    @param:ConfigProperty(name = "encryption.master-key", defaultValue = "ezfzefzefzef")
    private val masterKey: String,
) {
    private val secretKey: SecretKey by lazy {
        val keyBytes = masterKey.toByteArray().copyOf(32)
        SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plainText: String): String {
        val iv =
            ByteArray(12).apply {
                SecureRandom().nextBytes(this)
            }

        val gcmSpec = GCMParameterSpec(128, iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val encrypted = cipher.doFinal(plainText.toByteArray())

        // Combine IV + encrypted data
        val combined = iv + encrypted
        return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(encryptedText: String): String {
        val payload = encryptedText.removePrefix(ENCRYPTED_PREFIX)
        val combined = Base64.getDecoder().decode(payload)

        // Extract IV (first 12 bytes)
        val iv = combined.copyOfRange(0, 12)
        val encrypted = combined.copyOfRange(12, combined.size)

        val gcmSpec = GCMParameterSpec(128, iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted)
    }

    fun decryptIfEncrypted(value: String): String {
        if (value.startsWith(ENCRYPTED_PREFIX)) return decrypt(value)
        if ("://" in value) return value

        return runCatching { decrypt(value) }.getOrDefault(value)
    }

    fun isEncrypted(value: String): Boolean = value.startsWith(ENCRYPTED_PREFIX)

    private companion object {
        const val ENCRYPTED_PREFIX = "enc:v1:"
    }
}
