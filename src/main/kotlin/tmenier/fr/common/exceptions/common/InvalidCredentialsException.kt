package tmenier.fr.common.exceptions.common

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class InvalidCredentialsException : ApiException(
    "INVALID_CREDENTIALS",
    "Invalid email or password",
    Response.Status.UNAUTHORIZED,
)