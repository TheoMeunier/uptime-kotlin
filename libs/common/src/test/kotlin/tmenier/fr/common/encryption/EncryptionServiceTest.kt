package tmenier.fr.common.encryption

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import javax.crypto.AEADBadTagException

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
    fun `reads v2 values written with a previous master key`() {
        val rotated = EncryptionService(NEW_MASTER_KEY, Optional.empty(), Optional.of(listOf(MASTER_KEY)))
        val written = service.encrypt("smtp-password")

        assertEquals("smtp-password", rotated.decrypt(written))
        assertEquals("smtp-password", rotated.decrypt(V2_REFERENCE))
        assertFalse(rotated.isEncryptedWithCurrentKey(written))
    }

    @Test
    fun `re-encrypts values from a previous key or v1 with the current key`() {
        val rotated = EncryptionService(NEW_MASTER_KEY, Optional.of(MASTER_KEY), Optional.of(listOf(MASTER_KEY)))

        listOf(service.encrypt("smtp-password"), V1_REFERENCE).forEach { stored ->
            val reEncrypted = rotated.reEncrypt(stored)!!

            assertTrue(rotated.isEncryptedWithCurrentKey(reEncrypted))
            assertEquals("smtp-password", EncryptionService(NEW_MASTER_KEY).decrypt(reEncrypted))
        }
    }

    @Test
    fun `leaves values already encrypted with the current key untouched`() {
        assertNull(service.reEncrypt(service.encrypt("smtp-password")))
    }

    @Test
    fun `fails clearly when no configured key decrypts a v2 value`() {
        val rotated = EncryptionService(NEW_MASTER_KEY)

        val error = assertThrows<AEADBadTagException> { rotated.decrypt(service.encrypt("smtp-password")) }
        assertTrue(error.message!!.contains("ENCRYPTION_PREVIOUS_MASTER_KEYS"))
        assertThrows<AEADBadTagException> { rotated.reEncrypt(service.encrypt("smtp-password")) }
    }

    @Test
    fun `refuses a previous master key shorter than 32 bytes`() {
        val error =
            assertThrows<IllegalArgumentException> {
                EncryptionService(MASTER_KEY, Optional.empty(), Optional.of(listOf(SHORT_LEGACY_KEY)))
            }

        assertTrue(error.message!!.contains("ENCRYPTION_PREVIOUS_MASTER_KEYS"))
        assertFalse(error.message!!.contains(SHORT_LEGACY_KEY))
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
        const val NEW_MASTER_KEY = "a-brand-new-master-key-of-32-bytes!"
        const val SHORT_LEGACY_KEY = "zfzefzefzef1212zedazdazd"
        const val V2_REFERENCE = "enc:v2:AAAAAAAAAAAAAAAAVE14nuionOe5PsNerTixf6RDlF6PBWQoO4YaUt4="
        const val V1_REFERENCE = "enc:v1:AAAAAAAAAAAAAAAAvR7VcT9zPiucWu76fAW4zumBx/VRRnHCPA/5x+o="
        const val V1_SHORT_KEY_REFERENCE = "enc:v1:AAAAAAAAAAAAAAAAbk03nZusPfw71qdDUzk7XeajquKHVvWmy3hUibM="
    }
}
