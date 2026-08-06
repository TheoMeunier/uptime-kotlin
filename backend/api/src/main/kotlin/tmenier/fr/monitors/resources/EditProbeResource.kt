package tmenier.fr.monitors.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.mappers.ProbeContentMapper
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeRepository
import java.util.UUID

@Path("/api/probes/{probeId}/edit")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class EditProbeResource(
    private val probeRepository: ProbeRepository,
    private val encryptionService: EncryptionService,
) {
    @GET
    @Authenticated
    fun edit(
        @PathParam("probeId") probeId: String,
    ): Response {
        val uuid = UUID.fromString(probeId)

        val probeEntity =
            probeRepository.findByIdOrNull(uuid)
                ?: run {
                    logger.warn { "Probe edition requested for unknown probe $uuid" }
                    throw NotFoundException("Probe not found")
                }

        return Response
            .ok(
                ProbeMapper.toProbeWithNotificationsIdsDto(probeEntity) { content ->
                    ProbeContentMapper.revealSecrets(content, encryptionService::decryptIfEncrypted)
                },
            ).build()
    }
}
