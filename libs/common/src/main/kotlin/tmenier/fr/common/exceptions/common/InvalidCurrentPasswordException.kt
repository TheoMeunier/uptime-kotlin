package tmenier.fr.common.exceptions.common

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class InvalidCurrentPasswordException :
    ApiException(
        ERROR_CODE,
        "Current password is incorrect.",
        Response.Status.BAD_REQUEST,
    ) {
    companion object {
        const val ERROR_CODE = "INVALID_CURRENT_PASSWORD"
    }
}
