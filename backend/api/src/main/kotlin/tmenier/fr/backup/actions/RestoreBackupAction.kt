package tmenier.fr.backup.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.backup.dtos.BackupDocument
import tmenier.fr.backup.dtos.RestoreReport
import tmenier.fr.backup.exceptions.UnsupportedBackupFormatException
import tmenier.fr.backup.restore.RestoreStep
import tmenier.fr.backup.services.RestoreExecutor
import tmenier.fr.backup.services.RestorePlanner
import tmenier.fr.common.enums.backup.ConflictStrategy
import java.time.Instant

@ApplicationScoped
class RestoreBackupAction(
    private val planner: RestorePlanner,
    private val executor: RestoreExecutor,
) {
    @Transactional
    fun execute(
        document: BackupDocument,
        strategy: ConflictStrategy,
        dryRun: Boolean,
        now: Instant = Instant.now(),
    ): RestoreReport {
        checkFormat(document)

        val plan = planner.plan(document, strategy, now)
        if (!dryRun) executor.execute(plan)

        return RestoreReport(dryRun = dryRun, items = plan.steps.map(RestoreStep::toReport))
    }

    private fun checkFormat(document: BackupDocument) {
        val format = document.format ?: throw UnsupportedBackupFormatException("This is not an Uptime Kotlin backup")
        if (format > BackupDocument.FORMAT) {
            throw UnsupportedBackupFormatException(
                "This file was written by a newer version of Uptime Kotlin (format $format): update this instance first",
            )
        }
    }
}
