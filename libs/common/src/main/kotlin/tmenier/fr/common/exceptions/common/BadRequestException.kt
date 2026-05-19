package tmenier.fr.common.exceptions.common

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class BadRequestException(
    message: String?,
) : ApiException(
        "BAD_REQUEST",
        message ?: "Bad request.",
        Response.Status.BAD_REQUEST,
    )
