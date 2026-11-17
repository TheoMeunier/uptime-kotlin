package tmenier.fr.backup.exceptions

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class UnsupportedBackupFormatException(
    message: String,
) : ApiException(ERROR_CODE, message, Response.Status.BAD_REQUEST) {
    companion object {
        const val ERROR_CODE = "UNSUPPORTED_BACKUP_FORMAT"
    }
}
