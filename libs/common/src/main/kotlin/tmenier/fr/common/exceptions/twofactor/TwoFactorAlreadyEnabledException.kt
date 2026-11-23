package tmenier.fr.common.exceptions.twofactor

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class TwoFactorAlreadyEnabledException :
    ApiException(
        ERROR_CODE,
        "Two-factor authentication is already on.",
        Response.Status.CONFLICT,
    ) {
    companion object {
        const val ERROR_CODE = "TWO_FACTOR_ALREADY_ENABLED"
    }
}
