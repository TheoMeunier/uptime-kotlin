package tmenier.fr.backup.sections

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import tmenier.fr.common.enums.backup.BackupItemKind

@ApplicationScoped
class BackupSections(
    sections: Instance<BackupSection>,
) {
    val all: List<BackupSection> = sections.sortedBy { it.kind.ordinal }

    init {
        val missing = BackupItemKind.entries - all.map { it.kind }.toSet()
        check(missing.isEmpty()) { "No BackupSection for $missing" }
        check(all.size == BackupItemKind.entries.size) { "Several BackupSection for the same kind" }
    }

    fun of(kind: BackupItemKind): BackupSection = all.first { it.kind == kind }
}
