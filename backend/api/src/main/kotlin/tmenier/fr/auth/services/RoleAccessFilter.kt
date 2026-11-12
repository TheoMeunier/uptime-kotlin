package tmenier.fr.auth.services

import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.exceptions.core.ApiErrorResponse

@Provider
@Priority(Priorities.AUTHORIZATION)
class RoleAccessFilter : ContainerRequestFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        val security = requestContext.securityContext
        if (security?.userPrincipal == null) return

        val path = "/" + requestContext.uriInfo.path.trimStart('/')

        val passwordChangePending = security.isUserInRole(PASSWORD_CHANGE_REQUIRED_ROLE)
        if (passwordChangePending && !isAllowedWhilePasswordChangeRequired(requestContext.method, path)) {
            requestContext.abortWith(forbidden(PASSWORD_CHANGE_REQUIRED, "You must change your password before continuing.", path))
            return
        }

        val role =
            when {
                security.isUserInRole(UserRole.ADMIN_ROLE) -> UserRole.ADMIN
                security.isUserInRole(UserRole.VIEWER_ROLE) -> UserRole.VIEWER
                else -> null
            }

        if (isAllowed(role, requestContext.method, path)) return

        requestContext.abortWith(forbidden(ERROR_CODE, "Your account is read-only.", path))
    }

    private fun forbidden(
        code: String,
        message: String,
        path: String,
    ): Response =
        Response
            .status(Response.Status.FORBIDDEN)
            .type(MediaType.APPLICATION_JSON)
            .entity(ApiErrorResponse(error = code, message = message, path = path))
            .build()

    companion object {
        const val ERROR_CODE = "READ_ONLY_ACCOUNT"
        const val PASSWORD_CHANGE_REQUIRED = "PASSWORD_CHANGE_REQUIRED"
        const val PASSWORD_CHANGE_REQUIRED_ROLE = "password-change-required"

        private val PASSWORD_CHANGE_ALLOWED_WRITES =
            listOf(
                Regex("^/api/auth/(login|refresh|logout)/?$"),
                Regex("^/api/profile/update/password/?$"),
            )

        private val PASSWORD_CHANGE_ALLOWED_READS =
            listOf(
                Regex("^/api/app/status/?$"),
                Regex("^/api/version/?$"),
                Regex("^/api/status-feed/?$"),
                Regex("^/api/status/[^/]+(/(uptime|feed))?/?$"),
            )

        fun isAllowedWhilePasswordChangeRequired(
            method: String,
            path: String,
        ): Boolean =
            when {
                path.contains("..") -> false
                method.uppercase() in READ_METHODS -> PASSWORD_CHANGE_ALLOWED_READS.any { it.matches(path) }
                else -> PASSWORD_CHANGE_ALLOWED_WRITES.any { it.matches(path) }
            }

        private val READ_METHODS = setOf("GET", "HEAD", "OPTIONS")

        private val ADMIN_ONLY = Regex("^/api/users(/.*)?$")

        private val VIEWER_DENIED_READS =
            listOf(
                Regex("^/api/probes/[^/]+/edit/?$"),
            )

        private val VIEWER_ALLOWED_WRITES =
            listOf(
                Regex("^/api/auth/(login|refresh|logout)/?$"),
                Regex("^/api/auth/sessions/revoke-all/?$"),
                Regex("^/api/auth/sessions/[^/]+/?$"),
                Regex("^/api/profile/update(/password)?/?$"),
                Regex("^/api/profile/onboarding/complete/?$"),
            )

        fun isAllowed(
            role: UserRole?,
            method: String,
            path: String,
        ): Boolean =
            when (role) {
                UserRole.ADMIN -> true
                UserRole.VIEWER ->
                    when {
                        path.contains("..") || ADMIN_ONLY.matches(path) -> false
                        method.uppercase() in READ_METHODS -> VIEWER_DENIED_READS.none { it.matches(path) }
                        else -> VIEWER_ALLOWED_WRITES.any { it.matches(path) }
                    }

                null -> false
            }
    }
}
