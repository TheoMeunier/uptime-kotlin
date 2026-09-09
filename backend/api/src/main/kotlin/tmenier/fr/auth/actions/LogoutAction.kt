package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.dtos.requests.RefreshTokenRequest
import tmenier.fr.databases.repositories.RefreshTokenRepository
import java.util.UUID

/**
 * Server side logout: the refresh token presented by the client is deleted, so the session
 * cannot be resumed even if the token was copied elsewhere.
 *
 * Deliberately idempotent and silent: an unknown or malformed token yields the same answer as a
 * revoked one, so this endpoint cannot be used to probe which tokens exist.
 */
@ApplicationScoped
class LogoutAction(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun execute(payload: RefreshTokenRequest) {
        val token = runCatching { UUID.fromString(payload.refreshToken) }.getOrNull() ?: return

        refreshTokenRepository.revokeByRefreshToken(token)
    }
}
