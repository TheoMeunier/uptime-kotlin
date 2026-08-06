package tmenier.fr.databases.mappers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.enums.monitors.ProbeProtocol
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.databases.dtos.ProbeListDTO
import tmenier.fr.databases.dtos.ProbeMonitorDTO
import tmenier.fr.databases.dtos.ProbeShowDTO
import tmenier.fr.databases.dtos.ProbeStatusDTO
import tmenier.fr.databases.dtos.ProbeUptimeDTO
import tmenier.fr.databases.dtos.ProbeWithNotificationsDTO
import tmenier.fr.databases.dtos.ProbeWithNotificationsIdsDTO
import tmenier.fr.databases.dtos.StoreProbeDto
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.entities.ProbesMonitorsLogEntity

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

            ProbeProtocol.POSTGRESQL -> {
                objectMapper.treeToValue(probe.content, ProbeContent.PostgreSql::class.java)
            }

            ProbeProtocol.SQLSERVER -> {
                objectMapper.treeToValue(probe.content, ProbeContent.SqlServer::class.java)
            }

            ProbeProtocol.MYSQL -> {
                objectMapper.treeToValue(probe.content, ProbeContent.MySql::class.java)
            }

            ProbeProtocol.REDIS -> {
                objectMapper.treeToValue(probe.content, ProbeContent.Redis::class.java)
            }

            ProbeProtocol.SMTP -> {
                objectMapper.treeToValue(probe.content, ProbeContent.Smtp::class.java)
            }

            ProbeProtocol.KAFKA -> {
                objectMapper.treeToValue(probe.content, ProbeContent.Kafka::class.java)
            }

            ProbeProtocol.RABBITMQ -> {
                objectMapper.treeToValue(probe.content, ProbeContent.RabbitMq::class.java)
            }
        } as ProbeContent

    fun toEntity(content: ProbeContent): Pair<JsonNode, ProbeProtocol> {
        val type =
            when (content) {
                is ProbeContent.Http -> ProbeProtocol.HTTP
                is ProbeContent.Dns -> ProbeProtocol.DNS
                is ProbeContent.Tcp -> ProbeProtocol.TCP
                is ProbeContent.Ping -> ProbeProtocol.PING
                is ProbeContent.PostgreSql -> ProbeProtocol.POSTGRESQL
                is ProbeContent.SqlServer -> ProbeProtocol.SQLSERVER
                is ProbeContent.MySql -> ProbeProtocol.MYSQL
                is ProbeContent.Redis -> ProbeProtocol.REDIS
                is ProbeContent.Smtp -> ProbeProtocol.SMTP
                is ProbeContent.Kafka -> ProbeProtocol.KAFKA
                is ProbeContent.RabbitMq -> ProbeProtocol.RABBITMQ
            }

        val jsonNode = objectMapper.valueToTree<JsonNode>(content)

        return jsonNode to type
    }

    fun redactSecrets(content: ProbeContent): ProbeContent =
        when (content) {
            is ProbeContent.PostgreSql -> content.copy(connectionString = "")
            is ProbeContent.SqlServer -> content.copy(connectionString = "")
            is ProbeContent.MySql -> content.copy(connectionString = "")
            is ProbeContent.Redis -> content.copy(connectionString = "")
            is ProbeContent.RabbitMq -> content.copy(password = "")
            else -> content
        }

    fun revealSecrets(
        content: ProbeContent,
        decrypt: (String) -> String,
    ): ProbeContent =
        when (content) {
            is ProbeContent.PostgreSql -> content.copy(connectionString = decrypt(content.connectionString))
            is ProbeContent.SqlServer -> content.copy(connectionString = decrypt(content.connectionString))
            is ProbeContent.MySql -> content.copy(connectionString = decrypt(content.connectionString))
            is ProbeContent.Redis -> content.copy(connectionString = decrypt(content.connectionString))
            is ProbeContent.RabbitMq -> content.copy(password = decrypt(content.password))
            else -> content
        }

    fun toUrl(content: ProbeContent): String =
        when (content) {
            is ProbeContent.Http -> content.url
            is ProbeContent.Dns -> content.hostname
            is ProbeContent.Tcp -> "${content.url}:${content.tcpPort}"
            is ProbeContent.Ping -> content.ip
            is ProbeContent.PostgreSql -> content.host
            is ProbeContent.SqlServer -> content.host
            is ProbeContent.MySql -> content.host
            is ProbeContent.Redis -> content.host
            is ProbeContent.Smtp -> "${content.hostname}:${content.port}"
            is ProbeContent.Kafka -> content.brokers
            is ProbeContent.RabbitMq -> content.managementNodes
        }
}

object ProbeMapper {
    fun toEntity(dto: StoreProbeDto): ProbesEntity =
        ProbesEntity().apply {
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

    fun toDto(entity: ProbesEntity): ProbeDTO =
        ProbeDTO(
            id = entity.id,
            name = entity.name,
            interval = entity.interval,
            timeout = entity.timeout,
            retry = entity.retry,
            intervalRetry = entity.intervalRetry,
            enabled = entity.enabled,
            protocol = entity.protocol,
            description = entity.description,
            lastRun = entity.lastRun,
            status = entity.status,
            content = ProbeContentMapper.toDto(entity),
            url = ProbeContentMapper.toUrl(ProbeContentMapper.toDto(entity)),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

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

    fun toProbeWithNotificationsDto(entity: ProbesEntity): ProbeWithNotificationsDTO =
        ProbeWithNotificationsDTO(
            probe =
                ProbeDTO(
                    id = entity.id,
                    name = entity.name,
                    interval = entity.interval,
                    timeout = entity.timeout,
                    retry = entity.retry,
                    intervalRetry = entity.intervalRetry,
                    enabled = entity.enabled,
                    protocol = entity.protocol,
                    description = entity.description,
                    lastRun = entity.lastRun,
                    status = entity.status,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    content = ProbeContentMapper.redactSecrets(ProbeContentMapper.toDto(entity)),
                ),
            notifications = entity.notifications.map { NotificationMapper.toDto(it) },
        )

    fun toProbeWithNotificationsIdsDto(
        entity: ProbesEntity,
        contentMapper: (ProbeContent) -> ProbeContent = ProbeContentMapper::redactSecrets,
    ): ProbeWithNotificationsIdsDTO =
        ProbeWithNotificationsIdsDTO(
            probe =
                ProbeDTO(
                    id = entity.id,
                    name = entity.name,
                    interval = entity.interval,
                    timeout = entity.timeout,
                    retry = entity.retry,
                    intervalRetry = entity.intervalRetry,
                    enabled = entity.enabled,
                    protocol = entity.protocol,
                    description = entity.description,
                    lastRun = entity.lastRun,
                    status = entity.status,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                    content = contentMapper(ProbeContentMapper.toDto(entity)),
                ),
            notifications = entity.notifications.map { it.id },
        )

    fun toShowDto(
        entity: ProbesEntity,
        monitors: List<ProbesMonitorsLogEntity>,
        uptimes: ProbeUptimeDTO? = null,
    ): ProbeShowDTO {
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
                    protocol = entity.protocol,
                    description = entity.description,
                    lastRun = entity.lastRun,
                    status = entity.status,
                    content = ProbeContentMapper.redactSecrets(content),
                    url = ProbeContentMapper.toUrl(content),
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                ),
            monitors =
                monitors.map {
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
