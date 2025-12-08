package tmenier.fr.http.exceptions.core

import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import tmeunier.fr.http.exceptions.core.ApiErrorResponse

open class ApiException(
    val errorCode: String,
    message: String,
    val httpStatus: Response.Status = Response.Status.UNAUTHORIZED
) : RuntimeException(message)

@Provider
class ApiExceptionMapper : ExceptionMapper<ApiException> {

    @Context
    private lateinit var uriInfo: UriInfo

    override fun toResponse(exception: ApiException): Response {
        val error = exception.message?.let {
            ApiErrorResponse(
                error = exception.errorCode,
                message = it,
                path = uriInfo.path,
            )
        }

        return Response.status(exception.httpStatus)
            .entity(error)
            .type(MediaType.APPLICATION_JSON)
            .build()
    }
}