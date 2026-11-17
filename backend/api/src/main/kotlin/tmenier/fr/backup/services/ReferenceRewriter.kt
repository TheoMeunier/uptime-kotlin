package tmenier.fr.backup.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.backup.sections.ReferenceField
import tmenier.fr.backup.support.JsonTrees
import tmenier.fr.common.enums.backup.BackupItemKind
import java.util.UUID

data class UnknownReference(
    val target: BackupItemKind,
    val ref: String,
)

data class RewrittenItem(
    val item: ObjectNode,
    val unknown: List<UnknownReference>,
)

@ApplicationScoped
class ReferenceRewriter {
    fun rewrite(
        item: ObjectNode,
        fields: List<ReferenceField>,
        resolve: (BackupItemKind, String) -> UUID?,
    ): RewrittenItem {
        val copy = JsonTrees.copy(item)
        val unknown = mutableListOf<UnknownReference>()

        fields.forEach { field ->
            holders(copy, field).forEach { holder ->
                val refs = holder.remove(field.refsField) ?: return@forEach
                val ids =
                    refs
                        .map { it.asText() }
                        .mapNotNull { ref ->
                            resolve(field.target, ref).also { if (it == null) unknown += UnknownReference(field.target, ref) }
                        }.distinct()
                holder.set<JsonNode>(field.idsField, JsonTrees.array(ids.map(UUID::toString)))
            }
        }

        return RewrittenItem(copy, unknown)
    }

    private fun holders(
        item: ObjectNode,
        field: ReferenceField,
    ): List<ObjectNode> =
        when (val parent = field.parentArray) {
            null -> listOf(item)
            else -> item.path(parent).filterIsInstance<ObjectNode>()
        }
}
