package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.dtos.requests.RefreshTokenRequest
import tmenier.fr.auth.dtos.responses.LoginResponse
import tmenier.fr.auth.services.JwtService
import tmenier.fr.common.exceptions.common.InvalidCredentialsException
import tmenier.fr.databases.repositories.RefreshTokenRepository
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class RefreshTokenAction(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
) {
    fun execute(payload: RefreshTokenRequest): LoginResponse {
        val rt = refreshTokenRepository.findByRefreshToken(UUID.fromString(payload.refreshToken))

        if (rt.expiredAt.isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(rt)
            throw InvalidCredentialsException()
        }

        val newRefreshToken = jwtService.generateRefreshToken()
        val token = jwtService.generateJwt(rt.user.id, rt.user.name, rt.user.email)

        refreshTokenRepository.storeRefreshToken(newRefreshToken, rt.user)
        refreshTokenRepository.delete(rt)

        return LoginResponse(
            token,
            newRefreshToken.toString(),
        )
    }
}
