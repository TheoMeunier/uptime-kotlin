package tmenier.fr.monitors.resources

import io.quarkus.security.Authenticated
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.ProbeUptimeDTO
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.ProbeMonitorRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.time.LocalDateTime
import java.util.UUID

@Path("/api/probes/{probeId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ShowProbeResource(
    private val probeRepository: ProbeRepository,
    private val probeMonitorRepository: ProbeMonitorRepository,
) {
    @GET
    @Authenticated
    fun show(
        @PathParam("probeId") probeId: String,
        @QueryParam("hours") hours: Long,
    ): Response {
        val uuid = UUID.fromString(probeId)

        val probeEntity =
            probeRepository.findByIdOrNull(uuid)
                ?: run {
                    logger.warn { "Probe details requested for unknown probe $uuid" }
                    throw NotFoundException("Probe not found")
                }

        val validHours = setOf(1L, 3L, 6L, 24L, 168L)
        if (hours !in validHours) {
            throw BadRequestException("hours must be one of: 1, 3, 6, 24, 168")
        }

        val monitors =
            probeMonitorRepository.findByProbeAfter(
                probeId = uuid,
                after = LocalDateTime.now().minusHours(hours),
            )
        val uptimes = computeUptimes(uuid)

        return Response.ok(ProbeMapper.toShowDto(probeEntity, monitors, uptimes)).build()
    }

    private fun computeUptimes(probeId: UUID): ProbeUptimeDTO {
        val now = LocalDateTime.now()
        return ProbeUptimeDTO(
            h24 = computeUptime(probeId, now.minusHours(24), now),
            d7 = computeUptime(probeId, now.minusDays(7), now),
            d30 = computeUptime(probeId, now.minusDays(30), now),
        )
    }

    private fun computeUptime(
        probeId: UUID,
        from: LocalDateTime,
        to: LocalDateTime,
    ): Double {
        val total = probeMonitorRepository.countByProbeAndPeriod(probeId, from, to)
        val success = probeMonitorRepository.countSuccessByProbeAndPeriod(probeId, from, to)
        if (total == 0L) return 100.0
        return (success.toDouble() / total.toDouble()) * 100.0
    }
}
