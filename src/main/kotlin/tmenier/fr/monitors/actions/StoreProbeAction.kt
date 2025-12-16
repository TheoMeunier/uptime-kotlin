package tmenier.fr.monitors.actions

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.monitors.dtos.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.dtos.requests.ValidProbeProtocolDnsRequest
import tmenier.fr.monitors.dtos.requests.ValidProbeProtocolHttpRequest
import tmenier.fr.monitors.dtos.requests.ValidProbeProtocolPingRequest
import tmenier.fr.monitors.dtos.requests.ValidProbeProtocolTcpRequest
import tmenier.fr.monitors.entities.ProbesEntity
import java.util.UUID

@ApplicationScoped
class StoreProbeAction {

    fun execute(payload: BaseStoreProbeRequest) {
        val probe = ProbesEntity()
        probe.id = UUID.randomUUID()
        probe.name = payload.name!!
        probe.interval = payload.interval!!
        probe.intervalRetry = payload.intervalRetry!!
        probe.retry = payload.retry!!
        probe.protocol = payload.protocol!!
        probe.enabled = payload.enabled == true
        probe.description = payload.description
        probe.url = payload.url!!

        when (payload) {
            is ValidProbeProtocolHttpRequest -> {
                probe.notificationCertified = payload.notificationCertificate == true
                probe.ignoreCertificateErrors = payload.ignoreCertificateErrors == true
                probe.httpCodeAllowed = payload.httpCodeAllowed
            }

            is ValidProbeProtocolTcpRequest -> {
                probe.tcpPort = payload.tcpPort
            }

            is ValidProbeProtocolDnsRequest -> {
                probe.dnsPort = payload.dnsPort
                probe.dnsServer = payload.dnsServer
            }

            is ValidProbeProtocolPingRequest -> {
                probe.pingSize = payload.pingSize
                probe.pingMaxPacket = payload.pingMaxPacket
                probe.pingDelay = payload.pingDelay
                probe.pingNumericOutput = payload.pingNumericOutput
            }

            else -> {
                throw IllegalArgumentException("Invalid probe protocol: ${payload.protocol}")
            }
        }

        probe.persist()
    }
}