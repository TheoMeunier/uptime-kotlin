package tmenier.fr.auth.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.dtos.responses.LoginResponse
import tmenier.fr.databases.mappers.UserDto
import tmenier.fr.databases.repositories.RefreshTokenRepository

@ApplicationScoped
class SessionIssuer(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val sessionContextService: SessionContextService,
) {
    fun open(user: UserDto): LoginResponse {
        val refreshToken = jwtService.generateRefreshToken()
        val sessionId =
            refreshTokenRepository.storeRefreshToken(
                refreshToken,
                user,
                sessionContextService.userAgent(),
                sessionContextService.ipAddress(),
            )

        return LoginResponse(
            token = jwtService.generateJwt(user.id, user.name, user.email, user.role, user.mustChangePassword),
            refreshToken = refreshToken.toString(),
            sessionId = sessionId.toString(),
        )
    }
}
