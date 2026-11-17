package tmenier.fr.backup.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.backup.exceptions.RestoreAbortedException
import tmenier.fr.backup.restore.RestorePlan
import tmenier.fr.backup.sections.BackupSections

@ApplicationScoped
class RestoreExecutor(
    private val sections: BackupSections,
    private val referenceRewriter: ReferenceRewriter,
) {
    fun execute(plan: RestorePlan) {
        plan.writableSteps().forEach { step ->
            val section = sections.of(step.kind)
            val request = referenceRewriter.rewrite(requireNotNull(step.item), section.references, plan::createdId).item
            val id =
                runCatching { section.create(request) }
                    .getOrElse { throw RestoreAbortedException(step.kind, step.finalKey.orEmpty(), it) }
            step.created(id)
        }
    }
}
