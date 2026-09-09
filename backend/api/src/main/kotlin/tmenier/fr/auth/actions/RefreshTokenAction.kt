package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.dtos.requests.RefreshTokenRequest
import tmenier.fr.auth.dtos.responses.LoginResponse
import tmenier.fr.auth.services.JwtService
import tmenier.fr.auth.services.SessionContextService
import tmenier.fr.common.exceptions.common.InvalidCredentialsException
import tmenier.fr.databases.repositories.RefreshTokenRepository
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class RefreshTokenAction(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val sessionContextService: SessionContextService,
) {
    fun execute(payload: RefreshTokenRequest): LoginResponse {
        val submittedToken =
            runCatching { UUID.fromString(payload.refreshToken) }
                .getOrElse { throw InvalidCredentialsException() }

        val rt = refreshTokenRepository.findByRefreshToken(submittedToken)

        if (rt.expiredAt.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.revokeByRefreshToken(rt.token)
            throw InvalidCredentialsException()
        }

        val newRefreshToken = jwtService.generateRefreshToken()
        val token = jwtService.generateJwt(rt.user.id, rt.user.name, rt.user.email)


        refreshTokenRepository.rotateRefreshToken(
            rt.id,
            newRefreshToken,
            sessionContextService.userAgent() ?: rt.userAgent,
            sessionContextService.ipAddress() ?: rt.ipAddress,
        )

        return LoginResponse(
            token,
            newRefreshToken.toString(),
            rt.id.toString(),
        )
    }
}
