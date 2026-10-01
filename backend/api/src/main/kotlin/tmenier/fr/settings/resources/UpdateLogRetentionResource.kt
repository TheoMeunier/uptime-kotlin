package tmenier.fr.settings.resources

import io.quarkus.security.Authenticated
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.LogRetentionSettingsDto
import tmenier.fr.databases.repositories.InstanceSettingsRepository
import tmenier.fr.settings.dtos.requests.UpdateLogRetentionRequest

@Path("/api/settings/retention")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class UpdateLogRetentionResource(
    private val instanceSettingsRepository: InstanceSettingsRepository,
) {
    @PUT
    @Authenticated
    @Transactional
    fun update(
        @Valid payload: UpdateLogRetentionRequest,
    ): LogRetentionSettingsDto {
        val settings = instanceSettingsRepository.updateLogRetentionDays(payload.logRetentionDays)
        logger.info { "Default probe log retention set to ${settings.logRetentionDays ?: "unlimited"} day(s)" }

        return LogRetentionSettingsDto(settings.logRetentionDays)
    }
}
