package tmenier.fr.databases.mappers

import tmenier.fr.databases.dtos.ProbeCheckTaskDto
import tmenier.fr.databases.entities.ProbeCheckTaskEntity

object ProbeCheckTaskMapper {
    fun toDto(entity: ProbeCheckTaskEntity): ProbeCheckTaskDto =
        ProbeCheckTaskDto(
            id = entity.id,
            probeId = entity.probeId,
            region = entity.region,
            attemptNumber = entity.attemptNumber,
            status = entity.status,
            scheduledAt = entity.scheduledAt,
            availableAt = entity.availableAt,
            claimedBy = entity.claimedBy,
            leaseUntil = entity.leaseUntil,
            deliveryAttempts = entity.deliveryAttempts,
            maxDeliveryAttempts = entity.maxDeliveryAttempts,
            previousFailedAt = entity.previousFailedAt,
            resultMessage = entity.resultMessage,
            createdAt = entity.createdAt,
        )
}
