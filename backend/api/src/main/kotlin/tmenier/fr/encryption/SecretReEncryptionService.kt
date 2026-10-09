package tmenier.fr.encryption

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import io.quarkus.runtime.StartupEvent
import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.transaction.Transactional
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.AdvisoryLockRepository
import tmenier.fr.databases.repositories.EncryptedContentRepository
import tmenier.fr.databases.repositories.EncryptedContentTable
import tmenier.fr.databases.repositories.JwtSigningKeyRepository

@RegisterForReflection
data class ReEncryptionReport(
    val reEncrypted: Int,
    val unreadable: List<String>,
)

@ApplicationScoped
class SecretReEncryptionService(
    private val encryptionService: EncryptionService,
    private val contentRepository: EncryptedContentRepository,
    private val jwtSigningKeyRepository: JwtSigningKeyRepository,
    private val advisoryLockRepository: AdvisoryLockRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun reEncryptAll(): ReEncryptionReport? {
        if (!advisoryLockRepository.tryLock(AdvisoryLockRepository.SECRET_RE_ENCRYPTION)) return null

        var reEncrypted = 0
        val unreadable = mutableListOf<String>()

        EncryptedContentTable.entries.forEach { table ->
            contentRepository.findWithEncryptedValues(table).forEach { row ->
                val document = objectMapper.readTree(row.content)
                val changed = reEncryptValues(document, "${table.tableName} ${row.id}", unreadable)
                if (changed > 0 && contentRepository.replaceContent(table, row.id, row.content, objectMapper.writeValueAsString(document))) {
                    reEncrypted += changed
                }
            }
        }

        jwtSigningKeyRepository.find()?.privateKey?.let { stored ->
            val replacement = reEncryptValue(stored, "jwt_signing_keys/private_key", unreadable)
            if (replacement != null && jwtSigningKeyRepository.replacePrivateKey(stored, replacement)) reEncrypted++
        }

        return ReEncryptionReport(reEncrypted = reEncrypted, unreadable = unreadable)
    }

    private fun reEncryptValues(
        node: JsonNode,
        location: String,
        unreadable: MutableList<String>,
    ): Int {
        var changed = 0
        when (node) {
            is ObjectNode ->
                node.properties().map { it.key to it.value }.forEach { (name, child) ->
                    if (child.isTextual) {
                        reEncryptValue(child.asText(), "$location/$name", unreadable)?.let {
                            node.put(name, it)
                            changed++
                        }
                    } else {
                        changed += reEncryptValues(child, "$location/$name", unreadable)
                    }
                }

            is ArrayNode ->
                (0 until node.size()).forEach { index ->
                    val child = node.get(index)
                    if (child.isTextual) {
                        reEncryptValue(child.asText(), "$location[$index]", unreadable)?.let {
                            node.set(index, TextNode(it))
                            changed++
                        }
                    } else {
                        changed += reEncryptValues(child, "$location[$index]", unreadable)
                    }
                }
        }
        return changed
    }

    private fun reEncryptValue(
        value: String,
        location: String,
        unreadable: MutableList<String>,
    ): String? {
        if (!encryptionService.hasEncryptionMarker(value)) return null

        return runCatching { encryptionService.reEncrypt(value) }.getOrElse {
            unreadable += location
            null
        }
    }
}

@ApplicationScoped
class SecretReEncryptionStartup(
    private val service: SecretReEncryptionService,
) {
    fun onStart(
        @Observes event: StartupEvent,
    ) {
        val report =
            runCatching { service.reEncryptAll() }.getOrElse {
                logger.error(it) { "Re-encryption of the stored secrets failed, it will be retried on the next start" }
                return
            } ?: return

        if (report.reEncrypted > 0) {
            logger.info { "Re-encrypted ${report.reEncrypted} stored secret(s) with ENCRYPTION_MASTER_KEY" }
        }
        if (report.unreadable.isNotEmpty()) {
            logger.warn {
                "${report.unreadable.size} stored secret(s) cannot be decrypted with ENCRYPTION_MASTER_KEY, " +
                    "ENCRYPTION_PREVIOUS_MASTER_KEYS or ENCRYPTION_LEGACY_MASTER_KEY, re-enter them: " +
                    report.unreadable.joinToString()
            }
        } else if (report.reEncrypted > 0) {
            logger.info {
                "Every stored secret now uses ENCRYPTION_MASTER_KEY: ENCRYPTION_PREVIOUS_MASTER_KEYS and " +
                    "ENCRYPTION_LEGACY_MASTER_KEY can be removed once all instances run with the new key"
            }
        }
    }
}
