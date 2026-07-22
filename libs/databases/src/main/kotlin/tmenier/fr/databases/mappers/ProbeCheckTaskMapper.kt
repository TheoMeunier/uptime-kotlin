package tmenier.fr.databases.mappers

import tmenier.fr.databases.dtos.ProbeCheckTaskDto
import tmenier.fr.databases.entities.ProbeCheckTaskEntity

object ProbeCheckTaskMapper {

    fun toDto(entity: ProbeCheckTaskEntity): ProbeCheckTaskDto = ProbeCheckTaskDto(
        id = entity.id,
        probeId = entity.probeId,
        region = entity.region,
        attemptNumber = entity.attemptNumber,
        status = entity.status,
        scheduledAt = entity.scheduledAt,
        claimedBy = entity.claimedBy,
        claimedAt = entity.claimedAt,
        resultMessage = entity.resultMessage,
        createdAt = entity.createdAt
    )

}
