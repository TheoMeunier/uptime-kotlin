package tmenier.fr.backup.sections

import com.fasterxml.jackson.databind.node.ObjectNode
import tmenier.fr.backup.dtos.RestoreIssue
import tmenier.fr.backup.restore.RestoreContext
import tmenier.fr.common.enums.backup.BackupItemKind
import java.util.UUID

interface BackupSection {
    val kind: BackupItemKind

    val references: List<ReferenceField>

    fun backup(): List<ObjectNode>

    fun existingKeys(): Map<String, UUID>

    fun check(
        item: ObjectNode,
        context: RestoreContext,
    ): List<RestoreIssue>

    fun create(item: ObjectNode): UUID

    companion object {
        const val REF = "ref"
    }
}

data class ReferenceField(
    val refsField: String,
    val idsField: String,
    val target: BackupItemKind,
    val parentArray: String? = null,
)
