package tmenier.fr.common.exceptions.twofactor

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class TwoFactorSetupRequiredException :
    ApiException(
        ERROR_CODE,
        "Start the two-factor setup first.",
        Response.Status.CONFLICT,
    ) {
    companion object {
        const val ERROR_CODE = "TWO_FACTOR_SETUP_REQUIRED"
    }
}
