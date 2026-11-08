package tmenier.fr.settings.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import tmenier.fr.databases.repositories.InstanceSettingsRepository
import tmenier.fr.settings.dtos.DefaultStatusPageSettingsDto

@Path("/api/settings/status-page")
@Produces(MediaType.APPLICATION_JSON)
class ShowDefaultStatusPageResource(
    private val instanceSettingsRepository: InstanceSettingsRepository,
) {
    @GET
    @Authenticated
    fun show(): DefaultStatusPageSettingsDto = DefaultStatusPageSettingsDto(instanceSettingsRepository.isDefaultStatusPagePrivate())
}
