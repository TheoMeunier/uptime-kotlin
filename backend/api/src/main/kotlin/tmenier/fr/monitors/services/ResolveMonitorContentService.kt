package tmenier.fr.monitors.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolDnsRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolHttpRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPingRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolTcpRequest

@ApplicationScoped
class ResolveMonitorContentService {
    fun resolve(request: BaseStoreProbeRequest): ProbeContent =
        when (request) {
            is ValidProbeProtocolHttpRequest ->
                ProbeContent.Http(
                    url = request.url,
                    notificationCertified = request.notificationCertificate,
                    ignoreCertificateErrors = request.ignoreCertificateErrors,
                    httpCodeAllowed = request.httpCodeAllowed,
                )

            is ValidProbeProtocolTcpRequest ->
                ProbeContent.Tcp(
                    url = request.url,
                    tcpPort = request.tcpPort,
                )

            is ValidProbeProtocolDnsRequest ->
                ProbeContent.Dns(
                    hostname = request.hostname,
                    dnsPort = request.dnsPort,
                    dnsServer = request.dnsServer,
                    recordType = request.recordType,
                )

            is ValidProbeProtocolPingRequest ->
                ProbeContent.Ping(
                    ip = request.ip,
                    pingMaxPacket = request.pingMaxPacket,
                    pingSize = request.pingSize,
                    pingDelay = request.pingDelay,
                    pingNumericOutput = request.pingNumericOutput,
                )

            else -> throw IllegalArgumentException("Unsupported protocol")
        }
}
