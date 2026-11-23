package tmenier.fr.twofactor.actions

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.exceptions.twofactor.TwoFactorAlreadyEnabledException
import tmenier.fr.common.totp.TotpService
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.twofactor.dtos.responses.TwoFactorSetupResponse
import tmenier.fr.twofactor.services.TwoFactorVerifier
import java.util.UUID

@ApplicationScoped
class SetupTwoFactorAction(
    @ConfigProperty(name = "two-factor.issuer", defaultValue = "Uptime Kotlin") private val issuer: String,
    private val userRepository: UserRepository,
    private val verifier: TwoFactorVerifier,
    private val totpService: TotpService,
) {
    fun execute(userId: UUID): TwoFactorSetupResponse {
        val user = userRepository.findById(userId)
        if (user.totpEnabledAt != null) throw TwoFactorAlreadyEnabledException()

        val secret = verifier.startSetup(user)

        return TwoFactorSetupResponse(secret = secret, otpauthUri = totpService.provisioningUri(issuer, user.email, secret))
    }
}
