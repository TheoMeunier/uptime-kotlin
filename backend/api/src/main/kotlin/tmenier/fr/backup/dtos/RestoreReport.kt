package tmenier.fr.backup.dtos

import io.quarkus.runtime.annotations.RegisterForReflection
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.common.enums.backup.RestoreIssueCode
import tmenier.fr.common.enums.backup.RestoreOutcome

@RegisterForReflection
data class RestoreReport(
    val dryRun: Boolean,
    val items: List<RestoreItemReport>,
)

@RegisterForReflection
data class RestoreItemReport(
    val kind: BackupItemKind,
    val ref: String?,
    val name: String,
    val outcome: RestoreOutcome,
    val newName: String?,
    val issues: List<RestoreIssue>,
)

@RegisterForReflection
data class RestoreIssue(
    val code: RestoreIssueCode,
    val detail: String? = null,
    val target: BackupItemKind? = null,
)
