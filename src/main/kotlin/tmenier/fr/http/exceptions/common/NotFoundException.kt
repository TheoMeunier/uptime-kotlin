package tmenier.fr.http.exceptions.common

import jakarta.ws.rs.core.Response
import tmenier.fr.http.exceptions.core.ApiException

class NotFoundException(message: String?) : ApiException(
    "NOT_FOUND",
    message ?: "The requested resource was not found.",
    Response.Status.NOT_FOUND,
)