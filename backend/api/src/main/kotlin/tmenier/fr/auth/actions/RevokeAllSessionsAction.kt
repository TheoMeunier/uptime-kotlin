package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.databases.repositories.RefreshTokenRepository
import java.util.UUID

@ApplicationScoped
class RevokeAllSessionsAction(
    private val jwt: JsonWebToken,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun execute(): Long = refreshTokenRepository.revokeAllForUser(UUID.fromString(jwt.name))
}
