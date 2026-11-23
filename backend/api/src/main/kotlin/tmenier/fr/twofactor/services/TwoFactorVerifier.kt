package tmenier.fr.twofactor.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.security.RecoveryCodeService
import tmenier.fr.common.totp.TotpService
import tmenier.fr.databases.entities.UserEntity
import tmenier.fr.databases.repositories.RecoveryCodeRepository
import tmenier.fr.databases.repositories.UserRepository
import java.time.Instant

@ApplicationScoped
class TwoFactorVerifier(
    private val encryptionService: EncryptionService,
    private val totpService: TotpService,
    private val recoveryCodeService: RecoveryCodeService,
    private val userRepository: UserRepository,
    private val recoveryCodeRepository: RecoveryCodeRepository,
) {
    fun matchingStep(
        user: UserEntity,
        code: String,
        now: Instant = Instant.now(),
    ): Long? {
        val stored = user.totpSecret ?: return null

        return totpService.matchingStep(encryptionService.decrypt(stored), code, now, user.totpLastStep)
    }

    fun accept(
        user: UserEntity,
        code: String,
        allowRecoveryCode: Boolean,
        now: Instant = Instant.now(),
    ): Boolean {
        if (user.totpEnabledAt == null) return false

        val step = matchingStep(user, code, now)
        if (step != null) {
            userRepository.recordTotpStep(user, step)
            return true
        }

        return allowRecoveryCode && consumeRecoveryCode(user, code, now)
    }

    fun startSetup(user: UserEntity): String {
        val secret = totpService.generateSecret()
        userRepository.startTwoFactorSetup(user, encryptionService.encrypt(secret))
        return secret
    }

    fun regenerateRecoveryCodes(user: UserEntity): List<String> {
        val codes = recoveryCodeService.generate()
        recoveryCodeRepository.replaceAll(user.id, codes.map(recoveryCodeService::hash))
        return codes
    }

    fun disable(user: UserEntity) {
        userRepository.disableTwoFactor(user)
        recoveryCodeRepository.deleteAllForUser(user.id)
    }

    private fun consumeRecoveryCode(
        user: UserEntity,
        code: String,
        now: Instant,
    ): Boolean = recoveryCodeService.isWellFormed(code) && recoveryCodeRepository.consume(user.id, recoveryCodeService.hash(code), now)
}
