package tmenier.fr.monitors.entities.mapper

import tmenier.fr.monitors.dtos.responses.*
import tmenier.fr.monitors.entities.ProbesEntity

fun ProbesEntity.toShowDtp() =
    ProbeShowDTO(
        probe =
            ProbeDTO(
                id = id,
                name = name,
                url = url,
                interval = interval,
                timeout = timeout,
                retry = retry,
                intervalRetry = intervalRetry,
                enabled = enabled,
                protocol = protocol.name,
                description = description,
                lastRun = lastRun,
                status = status,
                notificationCertified = notificationCertified,
                ignoreCertificateErrors = ignoreCertificateErrors,
                httpCodeAllowed = httpCodeAllowed.map { it.name },
                tcpPort = tcpPort,
                dnsPort = dnsPort,
                dnsServer = dnsServer,
                pingMaxPacket = pingMaxPacket,
                pingSize = pingSize,
                pingDelay = pingDelay,
                pingNumericOutput = pingNumericOutput,
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
                status = status
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
