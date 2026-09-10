package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.dtos.requests.LoginRequest
import tmenier.fr.auth.dtos.responses.LoginResponse
import tmenier.fr.auth.services.JwtService
import tmenier.fr.auth.services.SessionContextService
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.common.InvalidCredentialsException
import tmenier.fr.databases.repositories.RefreshTokenRepository
import tmenier.fr.databases.repositories.UserRepository
import java.util.UUID

@ApplicationScoped
class LoginAction(
    val userRepository: UserRepository,
    val refreshTokenRepository: RefreshTokenRepository,
    val passwordService: BcryptService,
    val jwtService: JwtService,
    val sessionContextService: SessionContextService,
) {
    private val unknownUserHash: String by lazy {
        passwordService.hashPassword(UUID.randomUUID().toString())
    }

    fun execute(payload: LoginRequest): LoginResponse {
        val user = userRepository.findByEmailOrNull(payload.email)

        if (user == null) {
            passwordService.verifyPassword(payload.password, unknownUserHash)
            throw InvalidCredentialsException()
        }

        if (!passwordService.verifyPassword(payload.password, user.password)) {
            throw InvalidCredentialsException()
        }

        val refreshToken = jwtService.generateRefreshToken()
        val sessionId =
            refreshTokenRepository.storeRefreshToken(
                refreshToken,
                user,
                sessionContextService.userAgent(),
                sessionContextService.ipAddress(),
            )

        return LoginResponse(
            token = jwtService.generateJwt(user.id, user.name, user.email),
            refreshToken = refreshToken.toString(),
            sessionId = sessionId.toString(),
        )
    }
}
