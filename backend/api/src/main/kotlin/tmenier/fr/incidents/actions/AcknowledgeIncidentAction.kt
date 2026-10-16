package tmenier.fr.incidents.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.common.exceptions.core.ApiException
import tmenier.fr.databases.dtos.IncidentDto
import tmenier.fr.databases.mappers.IncidentMapper
import tmenier.fr.databases.repositories.IncidentRepository
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class AcknowledgeIncidentAction(
    private val incidentRepository: IncidentRepository,
) {
    @Transactional
    fun execute(
        incidentId: UUID,
        userId: UUID?,
    ): IncidentDto {
        incidentRepository.acknowledge(incidentId, userId, Instant.now())

        val incident = incidentRepository.findById(incidentId) ?: throw NotFoundException("Incident not found")
        if (incident.acknowledgedAt == null) {
            throw ApiException(INCIDENT_CLOSED, "A resolved incident can no longer be acknowledged.", Response.Status.CONFLICT)
        }

        return IncidentMapper.toDto(incident)
    }

    companion object {
        const val INCIDENT_CLOSED = "INCIDENT_CLOSED"
    }
}
