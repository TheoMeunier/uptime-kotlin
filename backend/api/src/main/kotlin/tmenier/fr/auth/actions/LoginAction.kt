package tmenier.fr.auth.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.auth.dtos.requests.LoginRequest
import tmenier.fr.auth.dtos.responses.LoginResponse
import tmenier.fr.auth.services.SessionIssuer
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.common.InvalidCredentialsException
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.twofactor.services.MfaChallengeService
import java.util.UUID

@ApplicationScoped
class LoginAction(
    val userRepository: UserRepository,
    val passwordService: BcryptService,
    val sessionIssuer: SessionIssuer,
    val mfaChallengeService: MfaChallengeService,
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

        if (user.twoFactorEnabled) {
            return LoginResponse(mfaRequired = true, challengeToken = mfaChallengeService.issue(user.id))
        }

        return sessionIssuer.open(user)
    }
}
