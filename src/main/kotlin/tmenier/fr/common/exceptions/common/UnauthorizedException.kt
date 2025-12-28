package tmenier.fr.common.exceptions.common

import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.core.ApiException

class UnauthorizedException :
    ApiException(
        "UNAUTHORIZED",
        "You are not authorized to perform this action.",
        Response.Status.UNAUTHORIZED,
    )
