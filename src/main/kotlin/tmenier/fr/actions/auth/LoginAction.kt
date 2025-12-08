package tmenier.fr.actions.auth

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.http.auth.dtos.requests.LoginRequest
import tmenier.fr.http.auth.dtos.responses.LoginResponse
import tmenier.fr.http.exceptions.common.InvalidCredentialsException
import tmenier.fr.http.exceptions.common.NotFoundException
import tmenier.fr.infrastructure.persistance.entity.RefreshTokenEntity
import tmenier.fr.infrastructure.persistance.entity.UserEntity
import tmenier.fr.services.JwtService
import tmenier.fr.services.PasswordService
import java.util.UUID

@ApplicationScoped
class LoginAction(
    val passwordService: PasswordService,
    val jwtService: JwtService
) {
    fun execute(payload: LoginRequest): LoginResponse {
        val user = UserEntity.findByEmail(payload.email)
            ?: throw NotFoundException("User not found with email: ${payload.email}")

        if (passwordService.verifyPassword(
                payload.password,
                user.password
            )
        ) {
            val refreshToken = UUID.randomUUID()

            RefreshTokenEntity().apply {
                id = UUID.randomUUID()
                this.user = user
                this.refreshToken = refreshToken
                expiredAt = java.time.LocalDateTime.now().plusDays(3)
            }.persist()

            return LoginResponse(
                token = jwtService.generateJwt(user.id),
                refreshToken = refreshToken.toString()
            )
        } else {
            passwordService.verifyPassword(payload.password, "uptime-kotlin")
        }

        throw InvalidCredentialsException()
    }
}