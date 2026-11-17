package tmenier.fr.backup.restore

import com.fasterxml.jackson.databind.node.ObjectNode
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.common.enums.backup.ConflictStrategy
import tmenier.fr.common.enums.backup.RestoreIssueCode
import java.util.UUID

class RestorePlan(
    val strategy: ConflictStrategy,
    private val existingKeys: Map<BackupItemKind, Map<String, UUID>>,
) {
    private val _steps = mutableListOf<RestoreStep>()
    val steps: List<RestoreStep> get() = _steps

    private val claimedKeys = BackupItemKind.entries.associateWith { mutableMapOf<String, RestoreStep>() }
    private val targets = BackupItemKind.entries.associateWith { mutableMapOf<String, RestoreTarget>() }
    private val placeholders = mutableMapOf<RestoreStep, UUID>()

    fun open(
        kind: BackupItemKind,
        ref: String?,
        key: String?,
    ): RestoreStep = RestoreStep(kind, ref, key).also(_steps::add)

    fun conflictOf(step: RestoreStep): RestoreTarget? {
        val key = step.key ?: return null
        existingKeys.getValue(step.kind)[key]?.let { return RestoreTarget.Existing(it) }
        return claimedKeys.getValue(step.kind)[key]?.let { RestoreTarget.Pending(it) }
    }

    fun skip(
        step: RestoreStep,
        holder: RestoreTarget,
    ) {
        step.skip(if (holder is RestoreTarget.Existing) RestoreIssueCode.ALREADY_EXISTS else RestoreIssueCode.DUPLICATE_IN_FILE)
        register(step, holder)
    }

    fun rename(step: RestoreStep) {
        val key = requireNotNull(step.key)
        val free =
            generateSequence(2) { it + 1 }
                .map { step.kind.copyKey(key, it) }
                .first { it !in existingKeys.getValue(step.kind) && it !in claimedKeys.getValue(step.kind) }
        step.renameTo(free)
    }

    fun accept(
        step: RestoreStep,
        request: ObjectNode,
    ) {
        step.accept(request)
        claimedKeys.getValue(step.kind)[requireNotNull(step.finalKey)] = step
        register(step, RestoreTarget.Pending(step))
    }

    fun writableSteps(): List<RestoreStep> = _steps.filter { it.outcome.writes }

    fun accepted(kind: BackupItemKind): List<ObjectNode> = writableSteps().filter { it.kind == kind }.mapNotNull { it.item }

    fun placeholderId(
        kind: BackupItemKind,
        ref: String,
    ): UUID? =
        when (val target = targets.getValue(kind)[ref]) {
            is RestoreTarget.Existing -> target.id
            is RestoreTarget.Pending -> placeholders.getOrPut(target.step) { UUID.randomUUID() }
            null -> null
        }

    fun createdId(
        kind: BackupItemKind,
        ref: String,
    ): UUID? = targets.getValue(kind)[ref]?.id

    private fun register(
        step: RestoreStep,
        target: RestoreTarget,
    ) {
        step.ref?.let { targets.getValue(step.kind).putIfAbsent(it, target) }
    }
}
