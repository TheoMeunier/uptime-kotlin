package tmenier.fr.monitors.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import tmenier.fr.databases.dtos.LogRetention
import tmenier.fr.databases.dtos.LogRetentionPreviewDto
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.settings.actions.PreviewLogRetentionAction
import java.util.UUID

@Path("/api/probes/{probeId}/logs/retention-preview")
@Produces(MediaType.APPLICATION_JSON)
class PreviewProbeLogRetentionResource(
    private val probeRepository: ProbeRepository,
    private val previewLogRetention: PreviewLogRetentionAction,
) {
    @GET
    @Authenticated
    fun preview(
        @PathParam("probeId") probeId: UUID,
        @QueryParam("days") days: Int?,
    ): LogRetentionPreviewDto {
        if (probeRepository.findByIdOrNull(probeId) == null) {
            throw NotFoundException("Probe not found")
        }
        if (!LogRetention.isValidOverride(days)) {
            throw BadRequestException("days must be empty, 0 or between ${LogRetention.MIN_DAYS} and ${LogRetention.MAX_DAYS}")
        }

        return previewLogRetention.forProbe(probeId, days)
    }
}
