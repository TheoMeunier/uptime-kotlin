package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.dtos.requests.LoginRequest
import tmenier.fr.auth.dtos.responses.LoginResponse
import tmenier.fr.auth.entities.RefreshTokenEntity
import tmenier.fr.auth.entities.UserEntity
import tmenier.fr.auth.services.JwtService
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.common.InvalidCredentialsException
import tmenier.fr.common.exceptions.common.NotFoundException
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class LoginAction(
    val passwordService: BcryptService,
    val jwtService: JwtService,
) {
    fun execute(payload: LoginRequest): LoginResponse {
        val user =
            UserEntity.findByEmail(payload.email)
                ?: throw NotFoundException("User not found with email: ${payload.email}")

        if (passwordService.verifyPassword(
                payload.password,
                user.password,
            )
        ) {
            val refreshToken = UUID.randomUUID()

            RefreshTokenEntity()
                .apply {
                    id = UUID.randomUUID()
                    this.user = user
                    this.refreshToken = refreshToken
                    expiredAt = LocalDateTime.now().plusDays(3)
                }.persist()

            return LoginResponse(
                token = jwtService.generateJwt(user.id, user.name, user.email),
                refreshToken = refreshToken.toString(),
            )
        } else {
            passwordService.verifyPassword(payload.password, "uptime-kotlin")
        }

        throw InvalidCredentialsException()
    }
}
