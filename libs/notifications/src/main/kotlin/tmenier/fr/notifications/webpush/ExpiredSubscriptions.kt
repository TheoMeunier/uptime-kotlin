package tmenier.fr.notifications.webpush

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.WebPushSubscriptionRepository

fun interface ExpiredSubscriptions {
    fun forget(endpoint: String)
}

@ApplicationScoped
class StoredExpiredSubscriptions(
    private val repository: WebPushSubscriptionRepository,
) : ExpiredSubscriptions {
    override fun forget(endpoint: String) {
        runCatching { repository.removeEndpoint(endpoint) }
            .onSuccess { if (it > 0) logger.info { "Removed an expired browser subscription from $it Web Push channel(s)" } }
            .onFailure { logger.warn { "Could not remove an expired browser subscription: ${it.message}" } }
    }
}
