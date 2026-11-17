package tmenier.fr.settings.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.InstanceSettingsRepository
import tmenier.fr.settings.dtos.DefaultStatusPageSettingsDto
import tmenier.fr.settings.dtos.requests.UpdateDefaultStatusPageRequest

@Path("/api/settings/status-page")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateDefaultStatusPageResource(
    private val instanceSettingsRepository: InstanceSettingsRepository,
) {
    @PUT
    @RolesAllowed(UserRole.ADMIN_ROLE)
    @Transactional
    fun update(payload: UpdateDefaultStatusPageRequest): DefaultStatusPageSettingsDto {
        val settings = instanceSettingsRepository.updateDefaultStatusPagePrivate(payload.isPrivate)
        logger.info { "Default status page set to ${if (settings.defaultStatusPagePrivate) "private" else "public"}" }

        return DefaultStatusPageSettingsDto(settings.defaultStatusPagePrivate)
    }
}
