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
import tmenier.fr.common.utils.MonitoringClock
import tmenier.fr.common.utils.logger
import tmenier.fr.databases.dtos.ProbeUptimeDTO
import tmenier.fr.databases.mappers.ProbeMapper
import tmenier.fr.databases.repositories.MaintenanceOccurrenceRepository
import tmenier.fr.databases.repositories.ProbeMonitorRepository
import tmenier.fr.databases.repositories.ProbeRepository
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Path("/api/probes/{probeId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ShowProbeResource(
    private val probeRepository: ProbeRepository,
    private val probeMonitorRepository: ProbeMonitorRepository,
    private val maintenanceOccurrenceRepository: MaintenanceOccurrenceRepository,
) {
    @GET
    @Authenticated
    fun show(
        @PathParam("probeId") probeId: UUID,
        @QueryParam("hours") hours: Long,
    ): Response {
        val probeEntity =
            probeRepository.findByIdOrNull(probeId)
                ?: run {
                    logger.warn { "Probe details requested for unknown probe $probeId" }
                    throw NotFoundException("Probe not found")
                }

        val validHours = setOf(1L, 3L, 6L, 24L, 168L)
        if (hours !in validHours) {
            throw BadRequestException("hours must be one of: 1, 3, 6, 24, 168")
        }

        val now = MonitoringClock.now()
        val from = now.minus(hours, ChronoUnit.HOURS)
        val monitors =
            probeMonitorRepository.findByProbeAfter(
                probeId = probeId,
                after = from,
            )
        val uptimes = computeUptimes(probeId)

        val maintenance =
            maintenanceOccurrenceRepository
                .findMaintenanceStateByProbe(
                    at = now,
                    horizon = now.plus(Duration.ofDays(MAINTENANCE_LOOKAHEAD_DAYS)),
                )[probeId]
        val maintenancePeriods =
            maintenanceOccurrenceRepository.findByProbeBetween(
                probeId = probeId,
                from = from,
                to = now,
            )

        return Response
            .ok(
                ProbeMapper.toShowDto(
                    entity = probeEntity,
                    monitors = monitors,
                    uptimes = uptimes,
                    maintenance = maintenance,
                    maintenancePeriods = maintenancePeriods,
                ),
            ).build()
    }

    private fun computeUptimes(probeId: UUID): ProbeUptimeDTO {
        val now = MonitoringClock.now()
        return ProbeUptimeDTO(
            h24 = computeUptime(probeId, now.minus(24, ChronoUnit.HOURS), now),
            d7 = computeUptime(probeId, now.minus(7, ChronoUnit.DAYS), now),
            d30 = computeUptime(probeId, now.minus(30, ChronoUnit.DAYS), now),
        )
    }

    private fun computeUptime(
        probeId: UUID,
        from: Instant,
        to: Instant,
    ): Double {
        val total = probeMonitorRepository.countByProbeAndPeriod(probeId, from, to)
        val success = probeMonitorRepository.countSuccessByProbeAndPeriod(probeId, from, to)
        if (total == 0L) return 100.0
        return (success.toDouble() / total.toDouble()) * 100.0
    }

    companion object {
        private const val MAINTENANCE_LOOKAHEAD_DAYS = 30L
    }
}
