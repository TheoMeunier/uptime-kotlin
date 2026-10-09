package tmenier.fr.deployments

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.encryption.SecretReEncryptionService
import java.util.UUID

class KeyRotationProfile :
    DeploymentProfile(
        mapOf(
            "scheduler.strategy" to "none",
            "scheduler.worker.name" to "it-key-rotation",
            "encryption.previous-master-keys" to SecretReEncryptionTest.PREVIOUS_MASTER_KEY,
        ),
    )

@QuarkusTest
@TestProfile(KeyRotationProfile::class)
class SecretReEncryptionTest {
    @Inject
    lateinit var service: SecretReEncryptionService

    @Inject
    lateinit var encryptionService: EncryptionService

    @Inject
    lateinit var fixtures: NotificationChannelFixtures

    private val created = mutableListOf<UUID>()

    @AfterEach
    fun cleanUp() {
        created.forEach(fixtures::delete)
    }

    @Test
    fun `secrets written with the previous master key are rewritten with the current one`() {
        val previous = EncryptionService(PREVIOUS_MASTER_KEY)
        val id = fixtures.create("""{"bot_token":"${previous.encrypt("bot-secret")}","chat_id":"42"}""").also(created::add)

        service.reEncryptAll()

        val token = fixtures.field(id, "bot_token")
        assertTrue(encryptionService.isEncryptedWithCurrentKey(token), token)
        assertEquals("bot-secret", encryptionService.decrypt(token))
        assertEquals("42", fixtures.field(id, "chat_id"))
    }

    @Test
    fun `secrets no configured key can decrypt are reported and left as they are`() {
        val stored = EncryptionService(UNKNOWN_MASTER_KEY).encrypt("bot-secret")
        val id = fixtures.create("""{"bot_token":"$stored","chat_id":"42"}""").also(created::add)

        val report = service.reEncryptAll()!!

        assertTrue("notifications_channels $id/bot_token" in report.unreadable, report.unreadable.toString())
        assertEquals(stored, fixtures.field(id, "bot_token"))
    }

    companion object {
        const val PREVIOUS_MASTER_KEY = "previous-master-key-of-at-least-32-bytes"
        const val UNKNOWN_MASTER_KEY = "unknown-master-key-of-at-least-32-bytes!"
    }
}

@ApplicationScoped
class NotificationChannelFixtures {
    @Inject
    lateinit var em: EntityManager

    @Transactional
    fun create(content: String): UUID =
        UUID.randomUUID().also { id ->
            em
                .createNativeQuery("INSERT INTO notifications_channels (id, name, content) VALUES (:id, 'it-key-rotation', CAST(:content AS jsonb))")
                .setParameter("id", id)
                .setParameter("content", content)
                .executeUpdate()
        }

    @Transactional
    fun field(
        id: UUID,
        name: String,
    ): String =
        em
            .createNativeQuery("SELECT content ->> :name FROM notifications_channels WHERE id = :id")
            .setParameter("name", name)
            .setParameter("id", id)
            .singleResult as String

    @Transactional
    fun delete(id: UUID) {
        em.createNativeQuery("DELETE FROM notifications_channels WHERE id = :id").setParameter("id", id).executeUpdate()
    }
}
