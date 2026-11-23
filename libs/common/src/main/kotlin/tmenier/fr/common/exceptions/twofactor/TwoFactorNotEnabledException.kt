package tmenier.fr.common.exceptions.twofactor

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class TwoFactorNotEnabledException :
    ApiException(
        ERROR_CODE,
        "Two-factor authentication is off.",
        Response.Status.CONFLICT,
    ) {
    companion object {
        const val ERROR_CODE = "TWO_FACTOR_NOT_ENABLED"
    }
}
