package tmenier.fr.backup.services

import com.fasterxml.jackson.databind.JsonNode
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.backup.support.JsonTrees
import tmenier.fr.common.encryption.EncryptionService

@ApplicationScoped
class BackupSecretsService(
    private val encryptionService: EncryptionService,
) {
    fun reveal(node: JsonNode): Int {
        var unreadable = 0
        JsonTrees.replaceTexts(node) { value ->
            if (!encryptionService.hasEncryptionMarker(value)) return@replaceTexts null
            runCatching { encryptionService.decrypt(value) }.getOrElse {
                unreadable++
                null
            }
        }
        return unreadable
    }
}
