package tmenier.fr.twofactor.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.exceptions.common.InvalidCurrentPasswordException
import tmenier.fr.common.exceptions.twofactor.InvalidTwoFactorCodeException
import tmenier.fr.common.exceptions.twofactor.TwoFactorNotEnabledException
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.twofactor.dtos.requests.DisableTwoFactorRequest
import tmenier.fr.twofactor.services.TwoFactorVerifier
import java.util.UUID

@ApplicationScoped
class DisableTwoFactorAction(
    private val userRepository: UserRepository,
    private val passwordService: BcryptService,
    private val verifier: TwoFactorVerifier,
) {
    fun execute(
        userId: UUID,
        payload: DisableTwoFactorRequest,
    ) {
        val user = userRepository.findById(userId)
        if (user.totpEnabledAt == null) throw TwoFactorNotEnabledException()

        if (!passwordService.verifyPassword(payload.password, user.password)) throw InvalidCurrentPasswordException()
        if (!verifier.accept(user, payload.code, allowRecoveryCode = true)) throw InvalidTwoFactorCodeException()

        verifier.disable(user)
    }
}
