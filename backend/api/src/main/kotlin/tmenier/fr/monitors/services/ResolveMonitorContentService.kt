package tmenier.fr.monitors.services

import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolDnsRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolHttpRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolMySqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPingRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPostgreSqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolTcpRequest
import java.net.URI

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
                    method = request.method,
                    headers = request.headers,
                    body = request.body,
                    authentication = request.authentication,
                    assertions = request.assertions,
                    followRedirects = request.followRedirects,
                    maxLatencyMs = request.maxLatencyMs,
                    tlsExpiryWarningDays = request.tlsExpiryWarningDays,
                    steps = request.steps,
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

            is ValidProbeProtocolPostgreSqlRequest ->
                ProbeContent.PostgreSql(
                    connectionString = request.connectionString,
                    host = databaseTarget(request.connectionString, 5432),
                    query = request.query,
                )

            is ValidProbeProtocolMySqlRequest ->
                ProbeContent.MySql(
                    connectionString = request.connectionString,
                    host = databaseTarget(request.connectionString, 3306),
                    query = request.query,
                )

            else -> throw IllegalArgumentException("Unsupported protocol")
        }

    private fun databaseTarget(
        connectionString: String,
        defaultPort: Int,
    ): String {
        val uri = URI(connectionString)
        val host = requireNotNull(uri.host) { "Database host is required" }
        val port = if (uri.port == -1) defaultPort else uri.port
        require(uri.path.isNotBlank() && uri.path != "/") { "Database name is required" }
        return "$host:$port${uri.path}"
    }
}
