package tmenier.fr.backup.restore

import com.fasterxml.jackson.databind.node.ObjectNode
import tmenier.fr.common.enums.backup.BackupItemKind
import java.time.Instant

class RestoreContext(
    val now: Instant,
    private val plan: RestorePlan,
) {
    fun accepted(kind: BackupItemKind): List<ObjectNode> = plan.accepted(kind)
}
