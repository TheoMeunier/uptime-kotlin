package tmenier.fr.monitors.entities.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import tmenier.fr.monitors.dtos.propbes.ProbeContent
import tmenier.fr.monitors.dtos.responses.*
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.enums.ProbeProtocol

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

            else -> {
                // TODO: create default probe content
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
}

fun ProbesEntity.toProbeWithNotificationsDTO() =
    ProbeWithNotificationsDTO(
        probe =
            ProbeDTO(
                id = id,
                name = name,
                interval = interval,
                timeout = timeout,
                retry = retry,
                intervalRetry = intervalRetry,
                enabled = enabled,
                protocol = protocol.name,
                description = description,
                lastRun = lastRun,
                status = status,
                createdAt = createdAt,
                updatedAt = updatedAt,
                content = ProbeContentMapper.toDto(this),
            ),
        notifications = notifications.map { it.id },
    )

fun ProbesEntity.toShowDtp() =
    ProbeShowDTO(
        probe =
            ProbeDTO(
                id = id,
                name = name,
                interval = interval,
                timeout = timeout,
                retry = retry,
                intervalRetry = intervalRetry,
                enabled = enabled,
                protocol = protocol.name,
                description = description,
                lastRun = lastRun,
                status = status,
                content = ProbeContentMapper.toDto(this),
                createdAt = createdAt,
                updatedAt = updatedAt,
            ),
        monitors =
            probesMonitorLogs.map {
                ProbeMonitorDTO(
                    id = it.id,
                    status = it.status,
                    responseTime = it.responseTime,
                    message = it.message,
                    runAt = it.runAt,
                )
            },
    )

fun ProbesEntity.toStatusDto() =
    ProbeStatusDTO(
        probe =
            ProbeListDTO(
                id = id,
                name = name,
                description = description,
                status = status,
            ),
        monitors =
            probesMonitorLogs.map {
                ProbeMonitorDTO(
                    id = it.id,
                    status = it.status,
                    responseTime = it.responseTime,
                    message = it.message,
                    runAt = it.runAt,
                )
            },
    )
