package tmenier.fr.backup.services

import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.backup.dtos.BackupDocument
import tmenier.fr.backup.dtos.RestoreIssue
import tmenier.fr.backup.restore.RestoreContext
import tmenier.fr.backup.restore.RestorePlan
import tmenier.fr.backup.sections.BackupSection
import tmenier.fr.backup.sections.BackupSections
import tmenier.fr.backup.support.JsonTrees
import tmenier.fr.common.enums.backup.ConflictStrategy
import tmenier.fr.common.enums.backup.RestoreIssueCode
import java.time.Instant

@ApplicationScoped
class RestorePlanner(
    private val sections: BackupSections,
    private val secretsService: BackupSecretsService,
    private val referenceRewriter: ReferenceRewriter,
) {
    fun plan(
        document: BackupDocument,
        strategy: ConflictStrategy,
        now: Instant,
    ): RestorePlan {
        val plan = RestorePlan(strategy, sections.all.associate { it.kind to it.existingKeys() })
        val context = RestoreContext(now, plan)

        sections.all.forEach { section ->
            document.items(section.kind).forEach { item -> planItem(section, item, plan, context) }
        }
        return plan
    }

    private fun planItem(
        section: BackupSection,
        item: ObjectNode,
        plan: RestorePlan,
        context: RestoreContext,
    ) {
        val step = plan.open(section.kind, ref = item.text(BackupSection.REF), key = item.text(section.kind.keyField))
        if (step.key == null) return step.report(RestoreIssueCode.MISSING_KEY)

        plan.conflictOf(step)?.let { holder ->
            when (plan.strategy) {
                ConflictStrategy.SKIP -> return plan.skip(step, holder)
                ConflictStrategy.RENAME -> plan.rename(step)
            }
        }

        val request = JsonTrees.copy(item)
        request.remove(BackupSection.REF)
        request.put(section.kind.keyField, step.finalKey)
        if (secretsService.reveal(request) > 0) return step.report(RestoreIssueCode.SECRET_UNREADABLE)

        val rewritten = referenceRewriter.rewrite(request, section.references, plan::placeholderId)
        step.report(rewritten.unknown.map { RestoreIssue(RestoreIssueCode.UNKNOWN_REFERENCE, detail = it.ref, target = it.target) })
        step.report(section.check(rewritten.item, context))

        if (!step.isRejected) plan.accept(step, request)
    }

    private fun ObjectNode.text(field: String): String? =
        path(field)
            .asText("")
            .trim()
            .ifEmpty { null }
}
