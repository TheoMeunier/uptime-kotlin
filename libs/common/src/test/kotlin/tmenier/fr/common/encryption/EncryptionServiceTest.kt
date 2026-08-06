package tmenier.fr.common.encryption

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EncryptionServiceTest {
    private val service = EncryptionService("0123456789abcdef0123456789abcdef")

    @Test
    fun `encrypts with a version marker and decrypts the value`() {
        val plainText = "postgres://monitor:secret@localhost/application"

        val encrypted = service.encrypt(plainText)

        assertNotEquals(plainText, encrypted)
        assertTrue(encrypted.startsWith("enc:v1:"))
        assertEquals(plainText, service.decrypt(encrypted))
    }

    @Test
    fun `still decrypts values written before the version marker`() {
        val plainText = "smtp-password"
        val legacyEncrypted = service.encrypt(plainText).removePrefix("enc:v1:")

        assertEquals(plainText, service.decrypt(legacyEncrypted))
        assertEquals(plainText, service.decryptIfEncrypted(legacyEncrypted))
    }

    @Test
    fun `leaves existing plain text connection strings readable during migration`() {
        val plainText = "redis://monitor:secret@localhost/0"

        assertEquals(plainText, service.decryptIfEncrypted(plainText))
    }
}
