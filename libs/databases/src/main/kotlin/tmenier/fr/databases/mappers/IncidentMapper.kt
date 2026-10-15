package tmenier.fr.databases.mappers

import tmenier.fr.databases.dtos.IncidentDto
import tmenier.fr.databases.entities.IncidentEntity

object IncidentMapper {
    fun toDto(entity: IncidentEntity): IncidentDto =
        IncidentDto(
            id = entity.id,
            probeId = entity.probe.id,
            probeName = entity.probe.name,
            startedAt = entity.startedAt,
            confirmedAt = entity.confirmedAt,
            endedAt = entity.endedAt,
            durationSeconds = entity.durationSeconds,
            openingMessage = entity.openingMessage,
            closedReason = entity.closedReason,
        )
}
