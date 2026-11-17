package tmenier.fr.backup

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import tmenier.fr.backup.restore.RestorePlan
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.common.enums.backup.ConflictStrategy
import tmenier.fr.common.enums.backup.RestoreIssueCode
import tmenier.fr.common.enums.backup.RestoreOutcome
import java.util.UUID

class RestorePlanTest {
    private val existingProbe = UUID.randomUUID()
    private val existing =
        BackupItemKind.entries.associateWith { emptyMap<String, UUID>() } +
            (BackupItemKind.PROBE to mapOf("API" to existingProbe, "API (2)" to UUID.randomUUID())) +
            (BackupItemKind.STATUS_PAGE to mapOf("public" to UUID.randomUUID()))

    @Test
    fun `an item whose key exists is skipped and its ref stands for the existing item`() {
        val plan = RestorePlan(ConflictStrategy.SKIP, existing)
        val step = plan.open(BackupItemKind.PROBE, ref = "p1", key = "API")

        plan.skip(step, requireNotNull(plan.conflictOf(step)))

        assertEquals(RestoreOutcome.SKIP, step.outcome)
        assertEquals(listOf(RestoreIssueCode.ALREADY_EXISTS), step.toReport().issues.map { it.code })
        assertEquals(existingProbe, plan.createdId(BackupItemKind.PROBE, "p1"))
    }

    @Test
    fun `a renamed copy takes the first free key, in the format of its kind`() {
        val plan = RestorePlan(ConflictStrategy.RENAME, existing)
        val probe = plan.open(BackupItemKind.PROBE, ref = "p1", key = "API")
        val page = plan.open(BackupItemKind.STATUS_PAGE, ref = "s1", key = "public")

        plan.rename(probe)
        plan.rename(page)

        assertEquals("API (3)", probe.finalKey)
        assertEquals("public-2", page.finalKey)
        assertEquals(RestoreOutcome.RENAME, probe.outcome)
    }

    @Test
    fun `a key accepted earlier in the file is a conflict too`() {
        val plan = RestorePlan(ConflictStrategy.SKIP, existing)
        val first = plan.open(BackupItemKind.NOTIFICATION, ref = "n1", key = "Ops")
        plan.accept(first, JsonNodeFactory.instance.objectNode())
        val second = plan.open(BackupItemKind.NOTIFICATION, ref = "n2", key = "Ops")

        plan.skip(second, requireNotNull(plan.conflictOf(second)))

        assertEquals(listOf(RestoreIssueCode.DUPLICATE_IN_FILE), second.toReport().issues.map { it.code })
        assertEquals(
            plan.placeholderId(BackupItemKind.NOTIFICATION, "n1"),
            plan.placeholderId(BackupItemKind.NOTIFICATION, "n2"),
            "both refs stand for the same channel",
        )
    }

    @Test
    fun `an item to create has a stand-in id until it is written`() {
        val plan = RestorePlan(ConflictStrategy.SKIP, existing)
        val step = plan.open(BackupItemKind.NOTIFICATION, ref = "n1", key = "Ops")
        plan.accept(step, JsonNodeFactory.instance.objectNode())

        assertNotNull(plan.placeholderId(BackupItemKind.NOTIFICATION, "n1"))
        assertNull(plan.createdId(BackupItemKind.NOTIFICATION, "n1"))

        val created = UUID.randomUUID()
        step.created(created)
        assertEquals(created, plan.createdId(BackupItemKind.NOTIFICATION, "n1"))
        assertNull(plan.placeholderId(BackupItemKind.NOTIFICATION, "unknown"))
    }

    @Test
    fun `a blocking issue rejects the item and cancels its renaming`() {
        val plan = RestorePlan(ConflictStrategy.RENAME, existing)
        val step = plan.open(BackupItemKind.PROBE, ref = "p1", key = "API")
        plan.rename(step)

        step.report(RestoreIssueCode.SECRET_UNREADABLE)

        assertEquals(RestoreOutcome.ERROR, step.outcome)
        assertNull(step.newKey)
        assertEquals(emptyList<Any>(), plan.writableSteps())
    }
}
