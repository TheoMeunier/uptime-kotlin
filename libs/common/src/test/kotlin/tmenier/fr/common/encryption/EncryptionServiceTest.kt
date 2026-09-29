package tmenier.fr.common.encryption

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class EncryptionServiceTest {
    private val service = EncryptionService(MASTER_KEY)

    @Test
    fun `encrypts with the v2 marker and decrypts the value`() {
        val plainText = "postgres://monitor:secret@localhost/application"

        val encrypted = service.encrypt(plainText)

        assertNotEquals(plainText, encrypted)
        assertTrue(encrypted.startsWith("enc:v2:"))
        assertTrue(service.isEncryptedWithCurrentKey(encrypted))
        assertEquals(plainText, service.decrypt(encrypted))
    }

    @Test
    fun `never reuses an IV`() {
        assertNotEquals(service.encrypt("same"), service.encrypt("same"))
    }

    @Test
    fun `round trips non ASCII text`() {
        val plainText = "mot-de-passe-éàü-日本"

        assertEquals(plainText, service.decrypt(service.encrypt(plainText)))
    }

    @Test
    fun `derives the key with HKDF-SHA256`() {
        // Ciphertext produced outside the JVM (Python `cryptography`, HKDF + AES-GCM, zero IV).
        assertEquals("smtp-password", service.decrypt(V2_REFERENCE))
    }

    @Test
    fun `still decrypts v1 values written with the zero-padded master key`() {
        assertEquals("smtp-password", service.decrypt(V1_REFERENCE))
        assertEquals("smtp-password", service.decryptIfEncrypted(V1_REFERENCE))
        assertFalse(service.isEncryptedWithCurrentKey(V1_REFERENCE))
    }

    @Test
    fun `still decrypts values written before the version marker`() {
        val unprefixed = V1_REFERENCE.removePrefix("enc:v1:")

        assertEquals("smtp-password", service.decrypt(unprefixed))
        assertEquals("smtp-password", service.decryptIfEncrypted(unprefixed))
    }

    @Test
    fun `reads v1 values with the legacy key when the master key was rotated`() {
        val rotated = EncryptionService("a-brand-new-master-key-of-32-bytes!", Optional.of(SHORT_LEGACY_KEY))

        assertEquals("smtp-password", rotated.decrypt(V1_SHORT_KEY_REFERENCE))
    }

    @Test
    fun `leaves existing plain text connection strings readable during migration`() {
        val plainText = "redis://monitor:secret@localhost/0"

        assertEquals(plainText, service.decryptIfEncrypted(plainText))
    }

    @Test
    fun `refuses a master key shorter than 32 bytes`() {
        val error = assertThrows<IllegalArgumentException> { EncryptionService(SHORT_LEGACY_KEY) }

        assertTrue(error.message!!.contains("at least 32 bytes"))
        assertFalse(error.message!!.contains(SHORT_LEGACY_KEY))
    }

    private companion object {
        const val MASTER_KEY = "0123456789abcdef0123456789abcdef"
        const val SHORT_LEGACY_KEY = "zfzefzefzef1212zedazdazd"
        const val V2_REFERENCE = "enc:v2:AAAAAAAAAAAAAAAAVE14nuionOe5PsNerTixf6RDlF6PBWQoO4YaUt4="
        const val V1_REFERENCE = "enc:v1:AAAAAAAAAAAAAAAAvR7VcT9zPiucWu76fAW4zumBx/VRRnHCPA/5x+o="
        const val V1_SHORT_KEY_REFERENCE = "enc:v1:AAAAAAAAAAAAAAAAbk03nZusPfw71qdDUzk7XeajquKHVvWmy3hUibM="
    }
}
