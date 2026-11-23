package tmenier.fr.common.exceptions.twofactor

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class InvalidTwoFactorCodeException(
    status: Response.Status = Response.Status.BAD_REQUEST,
) : ApiException(
        ERROR_CODE,
        "Invalid verification code.",
        status,
    ) {
    companion object {
        const val ERROR_CODE = "INVALID_TWO_FACTOR_CODE"

        fun atSignIn() = InvalidTwoFactorCodeException(Response.Status.UNAUTHORIZED)
    }
}
