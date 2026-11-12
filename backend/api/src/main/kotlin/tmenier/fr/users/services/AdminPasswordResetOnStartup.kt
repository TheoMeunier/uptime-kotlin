package tmenier.fr.users.services

import io.quarkus.narayana.jta.QuarkusTransaction
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import org.eclipse.microprofile.config.inject.ConfigProperty
import tmenier.fr.common.bcrypt.BcryptService
import tmenier.fr.common.config.DemoMode
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.InstanceSettingsRepository
import tmenier.fr.databases.repositories.RefreshTokenRepository
import tmenier.fr.databases.repositories.UserRepository
import tmenier.fr.users.services.AdminPasswordResetPolicy.Decision
import java.util.Optional

@ApplicationScoped
class AdminPasswordResetOnStartup(
    @ConfigProperty(name = "reset-admin-password") private val requestedEmail: Optional<String>,
    private val demoMode: DemoMode,
    private val userRepository: UserRepository,
    private val instanceSettingsRepository: InstanceSettingsRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val bcryptService: BcryptService,
    private val passwordGenerator: TemporaryPasswordGenerator,
) {
    fun onStart(
        @Observes event: StartupEvent,
    ) {
        if (demoMode.enabled) {
            if (requestedEmail.isPresent) logger.warn { "RESET_ADMIN_PASSWORD is ignored in demo mode." }
            return
        }

        try {
            QuarkusTransaction.requiringNew().run { apply() }
        } catch (e: Exception) {
            logger.error(e) { "RESET_ADMIN_PASSWORD: the reset failed, nothing was changed." }
        }
    }

    private fun apply() {
        val email = requestedEmail.orElse(null)?.trim()?.lowercase()
        val settings = instanceSettingsRepository.get()
        val user = email?.let { userRepository.findByEmailIgnoreCaseOrNull(it) }

        val decision =
            AdminPasswordResetPolicy.decide(
                requestedEmail = email,
                lastResetEmail = settings.adminPasswordResetEmail,
                accountFound = user != null,
                accountIsAdmin = user?.role == UserRole.ADMIN,
                passwordChangePending = user?.mustChangePassword == true,
            )

        when (decision) {
            Decision.NOTHING -> {
                Unit
            }

            Decision.CLEAR_MARKER -> {
                settings.adminPasswordResetEmail = null
            }

            Decision.UNKNOWN_ACCOUNT -> {
                logger.error { "RESET_ADMIN_PASSWORD: no account uses $email, nothing was changed." }
            }

            Decision.NOT_ADMIN -> {
                logger.error {
                    "RESET_ADMIN_PASSWORD: $email is not an admin, nothing was changed. " +
                        "An admin can reset this account from the users page."
                }
            }

            Decision.ALREADY_APPLIED -> {
                logger.warn {
                    "RESET_ADMIN_PASSWORD is still set but $email has already chosen a new password: " +
                        "remove the variable. To reset again, remove it, restart, then set it again."
                }
            }

            Decision.RESET -> {
                val password = passwordGenerator.generate()

                userRepository.setTemporaryPassword(user!!, bcryptService.hashPassword(password))
                refreshTokenRepository.revokeAllForUser(user.id)
                settings.adminPasswordResetEmail = email

                logger.warn {
                    """
                    |
                    |==================================================================
                    |  RESET_ADMIN_PASSWORD: the password of $email was reset.
                    |
                    |      Temporary password:  $password
                    |
                    |  Sign in with it, choose a new password, then remove
                    |  RESET_ADMIN_PASSWORD and restart.
                    |==================================================================
                    """.trimMargin()
                }
            }
        }
    }
}
