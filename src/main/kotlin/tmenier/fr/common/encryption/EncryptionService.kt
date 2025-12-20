package tmenier.fr.common.encryption

import io.quarkus.arc.Unremovable
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Unremovable
@ApplicationScoped
class EncryptionService {

    @ConfigProperty(name = "encryption.master-key", defaultValue = "ezfzefzefzef")
    private lateinit var masterKey: String

    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    private val secretKey: SecretKey by lazy {
        val keyBytes = masterKey.toByteArray().copyOf(32)
        SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plainText: String): String {
        val iv = ByteArray(12).apply {
            java.security.SecureRandom().nextBytes(this)
        }

        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val encrypted = cipher.doFinal(plainText.toByteArray())

        // Combine IV + encrypted data
        val combined = iv + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(encryptedText: String): String {
        val combined = Base64.getDecoder().decode(encryptedText)

        // Extract IV (first 12 bytes)
        val iv = combined.copyOfRange(0, 12)
        val encrypted = combined.copyOfRange(12, combined.size)

        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted)
    }
}