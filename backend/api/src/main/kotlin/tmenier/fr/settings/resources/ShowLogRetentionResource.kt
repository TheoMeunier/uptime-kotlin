package tmenier.fr.settings.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import tmenier.fr.databases.dtos.LogRetentionSettingsDto
import tmenier.fr.databases.repositories.InstanceSettingsRepository

@Path("/api/settings/retention")
@Produces(MediaType.APPLICATION_JSON)
class ShowLogRetentionResource(
    private val instanceSettingsRepository: InstanceSettingsRepository,
) {
    @GET
    @Authenticated
    fun show(): LogRetentionSettingsDto = LogRetentionSettingsDto(instanceSettingsRepository.defaultLogRetentionDays())
}
