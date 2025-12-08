package tmenier.fr.actions.auth

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.http.auth.dtos.requests.RefreshTokenRequest
import tmenier.fr.http.auth.dtos.responses.LoginResponse
import tmenier.fr.http.exceptions.common.InvalidCredentialsException
import tmenier.fr.infrastructure.persistance.entity.RefreshTokenEntity
import tmenier.fr.services.JwtService
import java.util.UUID

@ApplicationScoped
class RefreshTokenAction(
    private val jwtService: JwtService
) {
    fun execute(payload: RefreshTokenRequest): LoginResponse {
        val rt = RefreshTokenEntity.findByRefreshToken(UUID.fromString(payload.refreshToken)) ?: throw InvalidCredentialsException()

        if (rt.expiredAt.isBefore(java.time.LocalDateTime.now())) {
            rt.delete()
            throw InvalidCredentialsException()
        }

        val newRefreshToken = UUID.randomUUID()

        RefreshTokenEntity().apply {
            id = UUID.randomUUID()
            this.user = rt.user
            this.refreshToken = newRefreshToken
            expiredAt = java.time.LocalDateTime.now().plusDays(3)
        }.persist()

        return LoginResponse(
            jwtService.generateJwt(rt.user.id),
            newRefreshToken.toString()
        )
    }
}