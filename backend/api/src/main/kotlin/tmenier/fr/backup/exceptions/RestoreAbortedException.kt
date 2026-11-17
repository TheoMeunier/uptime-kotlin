package tmenier.fr.backup.exceptions

import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.backup.BackupItemKind
import tmenier.fr.common.exceptions.core.ApiException

class RestoreAbortedException(
    kind: BackupItemKind,
    name: String,
    cause: Throwable,
) : ApiException(
        ERROR_CODE,
        "Restore stopped on ${kind.name.lowercase()} \"$name\": ${cause.message ?: cause.javaClass.simpleName}. Nothing was restored.",
        Response.Status.BAD_REQUEST,
    ) {
    companion object {
        const val ERROR_CODE = "RESTORE_ABORTED"
    }
}
