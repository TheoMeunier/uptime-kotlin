package tmenier.fr.statuspages.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Event
import jakarta.transaction.Transactional
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.databases.dtos.StoreStatusPageDto
import tmenier.fr.databases.dtos.StoreStatusPageGroupDto
import tmenier.fr.databases.repositories.StatusPageRepository
import tmenier.fr.statuspages.domains.RequestHost
import tmenier.fr.statuspages.domains.StatusPagesChanged
import tmenier.fr.statuspages.requests.StoreStatusPageRequest
import java.util.UUID

@ApplicationScoped
class StoreStatusPageAction(
    private val statusPageRepository: StatusPageRepository,
    private val statusPagesChanged: Event<StatusPagesChanged>,
) {
    @Transactional
    fun execute(
        payload: StoreStatusPageRequest,
        statusPageId: UUID? = null,
        currentHost: String? = null,
    ): UUID {
        val slug = payload.slug.trim()
        val customDomain =
            payload.customDomain
                ?.trim()
                ?.lowercase()
                ?.removeSuffix(".")
                ?.ifEmpty { null }
        validate(payload, slug, statusPageId)
        validateCustomDomain(customDomain, payload.isPrivate, statusPageId, RequestHost.normalize(currentHost))

        val dto =
            StoreStatusPageDto(
                id = statusPageId ?: UUID.randomUUID(),
                slug = slug,
                title = payload.title.trim(),
                description = payload.description?.trim()?.ifEmpty { null },
                defaultLayout = payload.defaultLayout,
                uptimePeriod = payload.uptimePeriod,
                isPrivate = payload.isPrivate,
                groups =
                    payload.groups.map { group ->
                        StoreStatusPageGroupDto(
                            name = group.name?.trim()?.ifEmpty { null },
                            probeIds = group.probeIds,
                        )
                    },
                customDomain = customDomain,
            )

        if (statusPageId != null) statusPageRepository.update(dto) else statusPageRepository.save(dto)
        statusPagesChanged.fire(StatusPagesChanged())

        return dto.id
    }

    private fun validate(
        payload: StoreStatusPageRequest,
        slug: String,
        statusPageId: UUID?,
    ) {
        if (statusPageRepository.isSlugTaken(slug, excludingId = statusPageId)) {
            throw BadRequestException("The address /status/$slug is already used by another status page")
        }

        val probeIds = payload.groups.flatMap { it.probeIds }
        if (probeIds.size != probeIds.toSet().size) {
            throw BadRequestException("A monitor can only appear once on a status page")
        }
    }

    private fun validateCustomDomain(
        domain: String?,
        isPrivate: Boolean,
        statusPageId: UUID?,
        currentHost: String?,
    ) {
        if (domain == null) return

        if (!DOMAIN_PATTERN.matches(domain)) {
            throw BadRequestException("$domain is not a valid domain name (e.g. status.example.com)")
        }
        if (isPrivate) {
            throw BadRequestException("A private status page cannot have a custom domain")
        }
        if (domain == currentHost) {
            throw BadRequestException("$domain is the address of this instance and cannot serve a status page")
        }
        if (statusPageRepository.isCustomDomainTaken(domain, excludingId = statusPageId)) {
            throw BadRequestException("The domain $domain is already used by another status page")
        }
    }

    private companion object {
        /** Lower-case host name with at least one dot and an alphabetic TLD; IP addresses are refused. */
        val DOMAIN_PATTERN = Regex("^(?=.{1,253}$)([a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,63}$")
    }
}
