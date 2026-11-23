package tmenier.fr.common.exceptions.twofactor

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class TooManyTwoFactorAttemptsException :
    ApiException(
        ERROR_CODE,
        "Too many invalid codes. Wait 15 minutes, then sign in again.",
        Response.Status.TOO_MANY_REQUESTS,
    ) {
    companion object {
        const val ERROR_CODE = "TOO_MANY_TWO_FACTOR_ATTEMPTS"
    }
}
