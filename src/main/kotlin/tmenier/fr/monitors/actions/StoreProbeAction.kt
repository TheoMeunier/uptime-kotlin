package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.exceptions.common.NotFoundException
import tmenier.fr.monitors.dtos.propbes.ProbeContent
import tmenier.fr.monitors.dtos.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.dtos.requests.ValidProbeProtocolDnsRequest
import tmenier.fr.monitors.dtos.requests.ValidProbeProtocolHttpRequest
import tmenier.fr.monitors.dtos.requests.ValidProbeProtocolPingRequest
import tmenier.fr.monitors.dtos.requests.ValidProbeProtocolTcpRequest
import tmenier.fr.monitors.entities.NotificationsChannelEntity
import tmenier.fr.monitors.entities.ProbesEntity
import tmenier.fr.monitors.entities.mapper.ProbeContentMapper
import java.util.UUID

@ApplicationScoped
class StoreProbeAction {
    fun execute(
        payload: BaseStoreProbeRequest,
        probeId: UUID? = null,
    ) {
        val probe =
            probeId?.let {
                ProbesEntity.findById(it) ?: throw NotFoundException("Probe with id $probeId not found")
            } ?: ProbesEntity().apply { id = UUID.randomUUID() }

        probe.name = payload.name
        probe.interval = payload.interval!!
        probe.intervalRetry = payload.intervalRetry!!
        probe.retry = payload.retry!!
        probe.protocol = payload.protocol
        probe.enabled = payload.enabled == true
        probe.description = payload.description

        // save notification
        val notificationFromDb = NotificationsChannelEntity.findByIds(payload.notifications)
        probe.notifications.addAll(notificationFromDb)

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
                            hostname = payload.url,
                            dnsPort = payload.dnsPort,
                            dnsServer = payload.dnsServer,
                        ),
                    )

                probe.content = jsonNode
            }

            is ValidProbeProtocolPingRequest -> {
                val (jsonNode, _) =
                    ProbeContentMapper.toEntity(
                        ProbeContent.Ping(
                            ip = payload.url,
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
