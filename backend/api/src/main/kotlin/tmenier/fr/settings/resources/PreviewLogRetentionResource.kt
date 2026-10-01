package tmenier.fr.settings.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import tmenier.fr.databases.dtos.LogRetention
import tmenier.fr.databases.dtos.LogRetentionPreviewDto
import tmenier.fr.settings.actions.PreviewLogRetentionAction

@Path("/api/settings/retention/preview")
@Produces(MediaType.APPLICATION_JSON)
class PreviewLogRetentionResource(
    private val previewLogRetention: PreviewLogRetentionAction,
) {
    @GET
    @Authenticated
    fun preview(
        @QueryParam("days") days: Int?,
    ): LogRetentionPreviewDto {
        if (!LogRetention.isValidDefault(days)) {
            throw BadRequestException("days must be empty or between ${LogRetention.MIN_DAYS} and ${LogRetention.MAX_DAYS}")
        }

        return previewLogRetention.forInstanceDefault(days)
    }
}
