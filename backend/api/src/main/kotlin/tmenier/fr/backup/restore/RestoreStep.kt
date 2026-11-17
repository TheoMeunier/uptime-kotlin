package tmenier.fr.backup.restore

import com.fasterxml.jackson.databind.node.ObjectNode
import tmenier.fr.backup.dtos.RestoreIssue
import tmenier.fr.backup.dtos.RestoreItemReport
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.common.enums.backup.RestoreIssueCode
import tmenier.fr.common.enums.backup.RestoreOutcome
import java.util.UUID

class RestoreStep(
    val kind: BackupItemKind,
    val ref: String?,
    val key: String?,
) {
    var outcome: RestoreOutcome = RestoreOutcome.CREATE
        private set

    var newKey: String? = null
        private set

    var item: ObjectNode? = null
        private set

    var createdId: UUID? = null
        private set

    private val issues = mutableListOf<RestoreIssue>()

    val finalKey: String? get() = newKey ?: key

    val isRejected: Boolean get() = outcome == RestoreOutcome.ERROR

    fun report(found: Collection<RestoreIssue>) {
        issues += found
        if (found.any { it.code.blocking }) {
            outcome = RestoreOutcome.ERROR
            newKey = null
        }
    }

    fun report(code: RestoreIssueCode) = report(listOf(RestoreIssue(code)))

    fun skip(because: RestoreIssueCode) {
        outcome = RestoreOutcome.SKIP
        report(because)
    }

    fun renameTo(key: String) {
        outcome = RestoreOutcome.RENAME
        newKey = key
    }

    fun accept(request: ObjectNode) {
        check(!isRejected) { "A rejected item cannot be accepted" }
        item = request
    }

    fun created(id: UUID) {
        createdId = id
    }

    fun toReport() =
        RestoreItemReport(
            kind = kind,
            ref = ref,
            name = key ?: ref ?: "?",
            outcome = outcome,
            newName = newKey,
            issues = issues.toList(),
        )
}
