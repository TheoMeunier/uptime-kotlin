package tmenier.fr.actions.probes

import com.arjuna.ats.internal.arjuna.objectstore.jdbc.drivers.ibm_driver
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.enums.ProbeProtocol
import tmenier.fr.http.probes.dtos.requests.BaseStoreProbeRequest
import tmenier.fr.http.probes.dtos.requests.ValidProbeProtocolDnsRequest
import tmenier.fr.http.probes.dtos.requests.ValidProbeProtocolHttpRequest
import tmenier.fr.http.probes.dtos.requests.ValidProbeProtocolPingRequest
import tmenier.fr.http.probes.dtos.requests.ValidProbeProtocolTcpRequest
import tmenier.fr.infrastructure.persistance.entity.ProbesEntity
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