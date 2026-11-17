package tmenier.fr.settings.resources

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import tmenier.fr.common.enums.users.UserRole
import tmenier.fr.databases.dtos.LogRetentionSettingsDto
import tmenier.fr.databases.repositories.InstanceSettingsRepository

@Path("/api/settings/retention")
@Produces(MediaType.APPLICATION_JSON)
class ShowLogRetentionResource(
    private val instanceSettingsRepository: InstanceSettingsRepository,
) {
    @GET
    @RolesAllowed(UserRole.ADMIN_ROLE, UserRole.VIEWER_ROLE)
    fun show(): LogRetentionSettingsDto = LogRetentionSettingsDto(instanceSettingsRepository.defaultLogRetentionDays())
}
