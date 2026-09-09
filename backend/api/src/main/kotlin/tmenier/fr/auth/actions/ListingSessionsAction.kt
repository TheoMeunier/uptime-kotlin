package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.jwt.JsonWebToken
import tmenier.fr.auth.dtos.responses.SessionResponse
import tmenier.fr.databases.repositories.RefreshTokenRepository
import java.util.UUID

@ApplicationScoped
class ListingSessionsAction(
    private val jwt: JsonWebToken,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun execute(currentSessionId: UUID?): List<SessionResponse> {
        val userId = UUID.fromString(jwt.name)

        return refreshTokenRepository.findActiveByUser(userId).map {
            SessionResponse(
                id = it.id,
                createdAt = it.createdAt,
                lastUsedAt = it.lastUsedAt,
                expiredAt = it.expiredAt,
                userAgent = it.userAgent,
                ipAddress = it.ipAddress,
                current = it.id == currentSessionId,
            )
        }
    }
}
