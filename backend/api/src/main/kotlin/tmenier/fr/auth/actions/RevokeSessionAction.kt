package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.repositories.RefreshTokenRepository
import java.util.UUID

@ApplicationScoped
class RevokeSessionAction(
    private val jwt: JsonWebToken,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun execute(sessionId: UUID) {
        val userId = UUID.fromString(jwt.name)

        // Scoped to the caller: a session id belonging to somebody else is reported as unknown.
        if (!refreshTokenRepository.revokeByIdForUser(sessionId, userId)) {
            throw NotFoundException("Session not found")
        }
    }
}
