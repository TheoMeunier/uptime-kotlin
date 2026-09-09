package tmenier.fr.auth

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.RefreshTokenRepository

/**
 * Expired refresh tokens are already rejected at refresh time, but they stay in the table until
 * something removes them. This drops them daily so the active session list only ever shows
 * sessions that can actually be resumed.
 */
@ApplicationScoped
class RefreshTokenPurgeService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Scheduled(cron = "0 15 3 * * ?", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    @Transactional
    fun purgeExpiredRefreshTokens() {
        try {
            val purged = refreshTokenRepository.purgeExpired()

            if (purged > 0) {
                logger.info { "Purged $purged expired refresh token(s)" }
            }
        } catch (error: Exception) {
            logger.error(error) { "Failed to purge expired refresh tokens" }
        }
    }
}
