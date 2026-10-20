package tmenier.fr.statuspages.resources

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Request
import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.common.BadRequestException
import tmenier.fr.statuspages.services.PublicJsonResponder
import tmenier.fr.statuspages.services.StatusUptimeService
import java.time.DateTimeException
import java.time.ZoneId
import java.time.ZoneOffset

@Path("/api/status/{slug}/uptime")
@Produces(MediaType.APPLICATION_JSON)
class PublicStatusUptimeResource(
    private val statusUptimeService: StatusUptimeService,
    private val responder: PublicJsonResponder,
) {
    @GET
    fun uptime(
        @PathParam("slug") slug: String,
        @QueryParam("tz") tz: String?,
        @Context request: Request,
    ): Response {
        val zone =
            try {
                tz?.takeIf { it.isNotBlank() }?.let(ZoneId::of) ?: ZoneOffset.UTC
            } catch (e: DateTimeException) {
                throw BadRequestException("Unknown time zone: $tz")
            }

        return responder.respond(request, statusUptimeService.forPage(slug, zone))
    }
}
