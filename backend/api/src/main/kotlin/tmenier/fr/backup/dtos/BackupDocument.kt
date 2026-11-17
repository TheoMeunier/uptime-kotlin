package tmenier.fr.backup.dtos

import com.fasterxml.jackson.databind.node.ObjectNode
import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.common.enums.backup.SecretsMode
import java.time.Instant

@RegisterForReflection
data class BackupDocument(
    val format: Int? = null,
    val application: String? = null,
    val version: String? = null,
    val createdAt: Instant? = null,
    val secrets: SecretsMode? = null,
    val notifications: List<ObjectNode> = emptyList(),
    val probes: List<ObjectNode> = emptyList(),
    val maintenances: List<ObjectNode> = emptyList(),
    val statusPages: List<ObjectNode> = emptyList(),
    val announcements: List<ObjectNode> = emptyList(),
) {
    fun items(kind: BackupItemKind): List<ObjectNode> =
        when (kind) {
            BackupItemKind.NOTIFICATION -> notifications
            BackupItemKind.PROBE -> probes
            BackupItemKind.MAINTENANCE -> maintenances
            BackupItemKind.STATUS_PAGE -> statusPages
            BackupItemKind.ANNOUNCEMENT -> announcements
        }

    companion object {
        const val FORMAT = 1
        const val APPLICATION = "uptime-kotlin"

        fun of(
            version: String,
            createdAt: Instant,
            secrets: SecretsMode,
            items: Map<BackupItemKind, List<ObjectNode>>,
        ) = BackupDocument(
            format = FORMAT,
            application = APPLICATION,
            version = version,
            createdAt = createdAt,
            secrets = secrets,
            notifications = items[BackupItemKind.NOTIFICATION].orEmpty(),
            probes = items[BackupItemKind.PROBE].orEmpty(),
            maintenances = items[BackupItemKind.MAINTENANCE].orEmpty(),
            statusPages = items[BackupItemKind.STATUS_PAGE].orEmpty(),
            announcements = items[BackupItemKind.ANNOUNCEMENT].orEmpty(),
        )
    }
}
