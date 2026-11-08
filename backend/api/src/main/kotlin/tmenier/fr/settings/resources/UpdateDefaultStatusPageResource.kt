package tmenier.fr.settings.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.repositories.InstanceSettingsRepository
import tmenier.fr.settings.dtos.DefaultStatusPageSettingsDto
import tmenier.fr.settings.dtos.requests.UpdateDefaultStatusPageRequest

/** Viewers cannot reach this write: `RoleAccessFilter` only lets them through a fixed list of endpoints. */
@Path("/api/settings/status-page")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateDefaultStatusPageResource(
    private val instanceSettingsRepository: InstanceSettingsRepository,
) {
    @PUT
    @Authenticated
    @Transactional
    fun update(payload: UpdateDefaultStatusPageRequest): DefaultStatusPageSettingsDto {
        val settings = instanceSettingsRepository.updateDefaultStatusPagePrivate(payload.isPrivate)
        logger.info { "Default status page set to ${if (settings.defaultStatusPagePrivate) "private" else "public"}" }

        return DefaultStatusPageSettingsDto(settings.defaultStatusPagePrivate)
    }
}
