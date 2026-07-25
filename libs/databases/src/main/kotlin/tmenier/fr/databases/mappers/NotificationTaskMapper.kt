package tmenier.fr.databases.mappers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import tmenier.fr.common.dtos.ProbeResult
import tmenier.fr.databases.dtos.NotificationQueueDto
import tmenier.fr.databases.entities.NotificationTaskEntity

object NotificationTaskMapper {
    private val objectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .findAndRegisterModules()

    fun payloadToEntity(payload: ProbeResult): JsonNode = objectMapper.valueToTree(payload)

    fun payloadToDto(entity: NotificationTaskEntity): ProbeResult = objectMapper.treeToValue(entity.payload, ProbeResult::class.java)

    fun toDtoWithRelation(entity: NotificationTaskEntity): NotificationQueueDto =
        NotificationQueueDto(
            id = entity.id,
            probe = entity.probe.let { ProbeMapper.toDto(it) },
            notification = entity.notification.let { NotificationMapper.toDto(it) },
            taskId = entity.checkTaskId,
            status = entity.status,
            event = entity.event,
            payload = payloadToDto(entity),
            attemptCount = entity.attemptCount,
            maxAttempts = entity.maxAttempts,
        )
}
