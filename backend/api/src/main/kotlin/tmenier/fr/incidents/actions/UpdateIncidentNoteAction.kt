package tmenier.fr.incidents.actions

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.databases.dtos.IncidentDto
import tmenier.fr.databases.mappers.IncidentMapper
import tmenier.fr.databases.repositories.IncidentRepository
import tmenier.fr.incidents.requests.UpdateIncidentNoteRequest
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class UpdateIncidentNoteAction(
    private val incidentRepository: IncidentRepository,
) {
    @Transactional
    fun execute(
        incidentId: UUID,
        request: UpdateIncidentNoteRequest,
    ): IncidentDto {
        val note = request.note?.trim()?.takeIf { it.isNotEmpty() }
        if (!incidentRepository.updateNote(incidentId, note, Instant.now())) {
            throw NotFoundException("Incident not found")
        }

        val incident = incidentRepository.findById(incidentId) ?: throw NotFoundException("Incident not found")
        return IncidentMapper.toDto(incident)
    }
}
