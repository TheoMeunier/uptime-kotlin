package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.dtos.requests.RefreshTokenRequest
import tmenier.fr.auth.dtos.responses.LoginResponse
import tmenier.fr.auth.entities.RefreshTokenEntity
import tmenier.fr.auth.services.JwtService
import tmenier.fr.common.exceptions.common.InvalidCredentialsException
import java.time.LocalDateTime
import java.util.*

@ApplicationScoped
class RefreshTokenAction(
    private val jwtService: JwtService,
) {
    fun execute(payload: RefreshTokenRequest): LoginResponse {
        val rt =
            RefreshTokenEntity.findByRefreshToken(UUID.fromString(payload.refreshToken))
                ?: throw InvalidCredentialsException()

        if (rt.expiredAt.isBefore(LocalDateTime.now())) {
            rt.delete()
            throw InvalidCredentialsException()
        }

        val newRefreshToken = UUID.randomUUID()

        RefreshTokenEntity()
            .apply {
                id = UUID.randomUUID()
                this.user = rt.user
                this.refreshToken = newRefreshToken
                expiredAt = LocalDateTime.now().plusDays(3)
            }.persist()

        return LoginResponse(
            jwtService.generateJwt(rt.user.id, rt.user.name, rt.user.email),
            newRefreshToken.toString(),
        )
    }
}
