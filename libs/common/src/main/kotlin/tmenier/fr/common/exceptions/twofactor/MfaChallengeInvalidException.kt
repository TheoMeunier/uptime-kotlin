package tmenier.fr.common.exceptions.twofactor

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class MfaChallengeInvalidException :
    ApiException(
        ERROR_CODE,
        "This sign-in step has expired. Sign in again.",
        Response.Status.UNAUTHORIZED,
    ) {
    companion object {
        const val ERROR_CODE = "MFA_CHALLENGE_INVALID"
    }
}
