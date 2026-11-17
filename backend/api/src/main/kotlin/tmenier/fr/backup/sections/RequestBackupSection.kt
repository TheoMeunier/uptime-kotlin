package tmenier.fr.backup.sections

import com.fasterxml.jackson.databind.node.ObjectNode
import tmenier.fr.backup.dtos.RestoreIssue
import tmenier.fr.backup.restore.RestoreContext
import tmenier.fr.backup.services.RequestRead
import tmenier.fr.backup.services.RequestReader
import tmenier.fr.common.enums.backup.RestoreIssueCode
import java.util.UUID

abstract class RequestBackupSection<R : Any> : BackupSection {
    protected abstract val requestReader: RequestReader
    protected abstract val requestType: Class<R>
    protected open val validationGroups: Array<Class<*>> = emptyArray()

    override val references: List<ReferenceField> = emptyList()

    override fun check(
        item: ObjectNode,
        context: RestoreContext,
    ): List<RestoreIssue> =
        when (val read = requestReader.read(item, requestType, validationGroups)) {
            is RequestRead.Invalid -> read.errors.map(::invalid)
            is RequestRead.Valid -> rules(read.request, context).map(::invalid) + notes(read.request)
        }

    override fun create(item: ObjectNode): UUID = store(requestReader.require(item, requestType))

    protected open fun rules(
        request: R,
        context: RestoreContext,
    ): List<String> = emptyList()

    protected open fun notes(request: R): List<RestoreIssue> = emptyList()

    protected abstract fun store(request: R): UUID

    protected fun violationOf(block: () -> Unit): String? = runCatching(block).exceptionOrNull()?.let { it.message ?: it.javaClass.simpleName }

    private fun invalid(message: String) = RestoreIssue(RestoreIssueCode.INVALID, detail = message)
}
