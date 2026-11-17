package tmenier.fr.backup.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.backup.dtos.BackupDocument
import tmenier.fr.backup.sections.BackupSections
import tmenier.fr.backup.services.BackupSecretsService
import tmenier.fr.common.enums.backup.SecretsMode
import java.time.Instant

@ApplicationScoped
class CreateBackupAction(
    private val sections: BackupSections,
    private val secretsService: BackupSecretsService,
    @ConfigProperty(name = "uptime.version", defaultValue = "dev")
    private val version: String,
) {
    @Transactional
    fun execute(
        secrets: SecretsMode,
        now: Instant = Instant.now(),
    ): BackupDocument {
        val items =
            sections.all.associate { section ->
                section.kind to section.backup().onEach { if (secrets == SecretsMode.PLAIN) secretsService.reveal(it) }
            }
        return BackupDocument.of(version = version, createdAt = now, secrets = secrets, items = items)
    }
}
