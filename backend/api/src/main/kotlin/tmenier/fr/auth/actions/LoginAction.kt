package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.dtos.requests.LoginRequest
import tmenier.fr.auth.dtos.responses.LoginResponse
import tmenier.fr.auth.services.JwtService
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.common.InvalidCredentialsException
import tmenier.fr.databases.repositories.RefreshTokenRepository
import tmenier.fr.databases.repositories.UserRepository

@ApplicationScoped
class LoginAction(
    val userRepository: UserRepository,
    val refreshTokenRepository: RefreshTokenRepository,
    val passwordService: BcryptService,
    val jwtService: JwtService,
) {
    fun execute(payload: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(payload.email)

        if (passwordService.verifyPassword(
                payload.password,
                user.password,
            )
        ) {
            val refreshToken = jwtService.generateRefreshToken()
            refreshTokenRepository.storeRefreshToken(refreshToken, user)

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
