package tmenier.fr.auth.providers

import io.quarkus.security.ForbiddenException
import io.quarkus.security.identity.SecurityIdentity
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.exceptions.core.ApiErrorResponse

@Provider
class ForbiddenExceptionMapper(
    private val identity: SecurityIdentity,
) : ExceptionMapper<ForbiddenException> {
    @Context
    private lateinit var uriInfo: UriInfo

    override fun toResponse(exception: ForbiddenException): Response {
        val (code, message) =
            when {
                identity.hasRole(UserRole.PASSWORD_CHANGE_REQUIRED_ROLE) ->
                    PASSWORD_CHANGE_REQUIRED to "You must change your password before continuing."
                identity.hasRole(UserRole.VIEWER_ROLE) -> READ_ONLY_ACCOUNT to "Your account is read-only."
                else -> FORBIDDEN to "You are not allowed to do this."
            }

        return Response
            .status(Response.Status.FORBIDDEN)
            .type(MediaType.APPLICATION_JSON)
            .entity(ApiErrorResponse(error = code, message = message, path = uriInfo.path))
            .build()
    }

    companion object {
        const val READ_ONLY_ACCOUNT = "READ_ONLY_ACCOUNT"
        const val PASSWORD_CHANGE_REQUIRED = "PASSWORD_CHANGE_REQUIRED"
        const val FORBIDDEN = "FORBIDDEN"
    }
}
