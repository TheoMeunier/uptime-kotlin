package tmenier.fr.statuspages.services

import io.quarkus.security.identity.SecurityIdentity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.repositories.InstanceSettingsRepository
import tmenier.fr.databases.repositories.StatusPageRepository
import tmenier.fr.monitors.services.StatusSnapshotService.Companion.CACHE_POLICY
import tmenier.fr.monitors.services.StatusSnapshotService.Companion.PRIVATE_CACHE_POLICY

@ApplicationScoped
class StatusPageAccessGuard(
    private val statusPageRepository: StatusPageRepository,
    private val instanceSettingsRepository: InstanceSettingsRepository,
    private val identity: SecurityIdentity,
) {
    @Transactional
    fun ensureReadable(slug: String): String {
        val isPrivate = statusPageRepository.isPrivateBySlug(slug)

        if (isPrivate == null || (isPrivate && identity.isAnonymous)) {
            throw NotFoundException("Status page not found: $slug")
        }

        return if (isPrivate) PRIVATE_CACHE_POLICY else CACHE_POLICY
    }

    @Transactional
    fun ensureGlobalReadable() {
        if (identity.isAnonymous && instanceSettingsRepository.isDefaultStatusPagePrivate()) {
            throw NotFoundException("Status page not found")
        }
    }
}
