package tmenier.fr.common.exceptions.twofactor

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class OwnTwoFactorException :
    ApiException(
        ERROR_CODE,
        "Turn off your own two-factor authentication from your profile.",
        Response.Status.CONFLICT,
    ) {
    companion object {
        const val ERROR_CODE = "OWN_TWO_FACTOR"
    }
}
