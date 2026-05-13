package tmenier.fr.databases.mappers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeContent
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.dtos.ProbeListDTO
import tmenier.fr.databases.dtos.ProbeMonitorDTO
import tmenier.fr.databases.dtos.ProbeShowDTO
import tmenier.fr.databases.dtos.ProbeStatusDTO
import tmenier.fr.databases.dtos.ProbeUptimeDTO
import tmenier.fr.databases.dtos.ProbeWithNotificationsDTO
import tmenier.fr.databases.dtos.StoreProbeDto
import tmenier.fr.databases.entities.ProbesEntity

object ProbeContentMapper {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun toDto(probe: ProbesEntity): ProbeContent =
        when (probe.protocol) {
            ProbeProtocol.HTTP -> {
                objectMapper.treeToValue(probe.content, ProbeContent.Http::class.java)
            }

            ProbeProtocol.DNS -> {
                objectMapper.treeToValue(probe.content, ProbeContent.Dns::class.java)
            }

            ProbeProtocol.TCP -> {
                objectMapper.treeToValue(probe.content, ProbeContent.Tcp::class.java)
            }

            ProbeProtocol.PING -> {
                objectMapper.treeToValue(probe.content, ProbeContent.Ping::class.java)
            }
        } as ProbeContent

    fun toEntity(content: ProbeContent): Pair<JsonNode, ProbeProtocol> {
        val type =
            when (content) {
                is ProbeContent.Http -> ProbeProtocol.HTTP
                is ProbeContent.Dns -> ProbeProtocol.DNS
                is ProbeContent.Tcp -> ProbeProtocol.TCP
                is ProbeContent.Ping -> ProbeProtocol.PING
            }

        val jsonNode = objectMapper.valueToTree<JsonNode>(content)

        return jsonNode to type
    }

    fun toUrl(content: ProbeContent): String =
        when (content) {
            is ProbeContent.Http -> content.url
            is ProbeContent.Dns -> content.hostname
            is ProbeContent.Tcp -> "${content.url}:${content.tcpPort}"
            is ProbeContent.Ping -> content.ip
        }
}

object ProbeMapper {

    fun toEntity(dto: StoreProbeDto): ProbesEntity = ProbesEntity().apply {
        id = dto.id
        name = dto.name
        interval = dto.interval
        retry = dto.retry
        intervalRetry = dto.intervalRetry
        enabled = dto.enabled
        protocol = dto.protocol
        description = dto.description
        content = ProbeContentMapper.toEntity(dto.content).first
    }

    fun toProbeListDto(entity: ProbesEntity): ProbeListDTO {
        val content = ProbeContentMapper.toDto(entity)

        return ProbeListDTO(
            id = entity.id,
            name = entity.name,
            url = ProbeContentMapper.toUrl(content),
            description = entity.description,
            status = entity.status,
        )
    }

    fun toProbeWithNotificationsDto(entity: ProbesEntity): ProbeWithNotificationsDTO {
        return ProbeWithNotificationsDTO(
            probe =
                ProbeDTO(
                    id = entity.id,
                    name = entity.name,
                    interval = entity.interval,
                    timeout = entity.timeout,
                    retry = entity.retry,
                    intervalRetry = entity.intervalRetry,
                    enabled = entity.enabled,
                    protocol = entity.protocol.name,
                    description = entity.description,
                    lastRun = entity.lastRun,
                    status = entity.status,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    content = ProbeContentMapper.toDto(entity),
                ),
            notifications = entity.notifications.map { NotificationMapper.toDto(it) },
        )
    }

    fun toShowDto(entity: ProbesEntity, uptimes: ProbeUptimeDTO? = null): ProbeShowDTO {
        val content = ProbeContentMapper.toDto(entity)

        return ProbeShowDTO(
            probe =
                ProbeDTO(
                    id = entity.id,
                    name = entity.name,
                    interval = entity.interval,
                    timeout = entity.timeout,
                    retry = entity.retry,
                    intervalRetry = entity.intervalRetry,
                    enabled = entity.enabled,
                    protocol = entity.protocol.name,
                    description = entity.description,
                    lastRun = entity.lastRun,
                    status = entity.status,
                    content = ProbeContentMapper.toDto(entity),
                    url = ProbeContentMapper.toUrl(content),
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                ),
            monitors =
                entity.probesMonitorLogs.map {
                    ProbeMonitorDTO(
                        id = it.id,
                        status = it.status,
                        responseTime = it.responseTime,
                        message = it.message,
                        runAt = it.runAt,
                    )
                },
            uptimes = uptimes,
        )
    }

    fun toStatusDto(entity: ProbesEntity): ProbeStatusDTO {
        val content = ProbeContentMapper.toDto(entity)

        return ProbeStatusDTO(
            probe =
                ProbeListDTO(
                    id = entity.id,
                    name = entity.name,
                    url = ProbeContentMapper.toUrl(content),
                    description = entity.description,
                    status = entity.status,
                ),
            monitors =
                entity.probesMonitorLogs.map {
                    ProbeMonitorDTO(
                        id = it.id,
                        status = it.status,
                        responseTime = it.responseTime,
                        message = it.message,
                        runAt = it.runAt,
                    )
                },
        )
    }
}

