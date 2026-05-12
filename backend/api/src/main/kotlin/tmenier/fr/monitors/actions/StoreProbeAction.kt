package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.databases.dtos.ProbeContent
import tmenier.fr.databases.entities.ProbesEntity
import tmenier.fr.databases.mappers.ProbeContentMapper
import tmenier.fr.databases.repositories.NotificationRepository
import tmenier.fr.databases.repositories.ProbeRepository
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolDnsRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolHttpRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPingRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolTcpRequest
import java.util.UUID

@ApplicationScoped
class StoreProbeAction(
    private val notificationRepository: NotificationRepository,
    private val probeRepository: ProbeRepository
) {
    fun execute(
        payload: BaseStoreProbeRequest,
        probeId: UUID? = null,
    ) {
        val probe =
            probeId?.let {
                probeRepository.findById(it)
            } ?: ProbesEntity().apply { id = UUID.randomUUID() }

        probe.name = payload.name
        probe.interval = payload.interval!!
        probe.intervalRetry = payload.intervalRetry!!
        probe.retry = payload.retry!!
        probe.protocol = payload.protocol
        probe.enabled = payload.enabled == true
        probe.description = payload.description

        // save notification
        val notificationFromDb = notificationRepository.findByIds(payload.notifications)
        probeRepository.attach(notificationFromDb, probe)


        when (payload) {
            is ValidProbeProtocolHttpRequest -> {
                val (jsonNode, _) =
                    ProbeContentMapper.toEntity(
                        ProbeContent.Http(
                            url = payload.url,
                            notificationCertified = payload.notificationCertificate,
                            ignoreCertificateErrors = payload.ignoreCertificateErrors,
                            httpCodeAllowed = payload.httpCodeAllowed,
                        ),
                    )

                probe.content = jsonNode
            }

            is ValidProbeProtocolTcpRequest -> {
                val (jsonNode, _) =
                    ProbeContentMapper.toEntity(
                        ProbeContent.Tcp(
                            url = payload.url,
                            tcpPort = payload.tcpPort,
                        ),
                    )

                probe.content = jsonNode
            }

            is ValidProbeProtocolDnsRequest -> {
                val (jsonNode, _) =
                    ProbeContentMapper.toEntity(
                        ProbeContent.Dns(
                            hostname = payload.hostname,
                            dnsPort = payload.dnsPort,
                            dnsServer = payload.dnsServer,
                            recordType = payload.recordType,
                        ),
                    )

                probe.content = jsonNode
            }

            is ValidProbeProtocolPingRequest -> {
                val (jsonNode, _) =
                    ProbeContentMapper.toEntity(
                        ProbeContent.Ping(
                            ip = payload.ip,
                            pingMaxPacket = payload.pingMaxPacket,
                            pingSize = payload.pingSize,
                            pingDelay = payload.pingDelay,
                            pingNumericOutput = payload.pingNumericOutput,
                        ),
                    )

                probe.content = jsonNode
            }

            else -> {
                throw IllegalArgumentException("Invalid probe protocol: ${payload.protocol}")
            }
        }

        probe.persist()
    }
}
