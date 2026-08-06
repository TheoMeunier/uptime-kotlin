package tmenier.fr.monitors.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolDnsRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolHttpRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolKafkaRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolMySqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPingRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPostgreSqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolRabbitMqRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolRedisRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolSmtpRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolSqlServerRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolTcpRequest
import java.net.URI

@ApplicationScoped
class ResolveMonitorContentService(
    private val encryptionService: EncryptionService,
) {
    fun resolve(
        request: BaseStoreProbeRequest,
        existingProbe: ProbeDTO? = null,
    ): ProbeContent =
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

            is ValidProbeProtocolPostgreSqlRequest -> {
                val connection =
                    resolveConnection(
                        incoming = request.connectionString,
                        existing = (existingProbe?.content as? ProbeContent.PostgreSql)?.connectionString,
                        kind = ConnectionKind.POSTGRESQL,
                    )
                ProbeContent.PostgreSql(
                    connectionString = connection.encrypted,
                    host = connection.target,
                    query = request.query,
                )
            }

            is ValidProbeProtocolSqlServerRequest -> {
                val connection =
                    resolveConnection(
                        incoming = request.connectionString,
                        existing = (existingProbe?.content as? ProbeContent.SqlServer)?.connectionString,
                        kind = ConnectionKind.SQL_SERVER,
                    )
                ProbeContent.SqlServer(
                    connectionString = connection.encrypted,
                    host = connection.target,
                    query = request.query,
                )
            }

            is ValidProbeProtocolMySqlRequest -> {
                val connection =
                    resolveConnection(
                        incoming = request.connectionString,
                        existing = (existingProbe?.content as? ProbeContent.MySql)?.connectionString,
                        kind = ConnectionKind.MYSQL,
                    )
                ProbeContent.MySql(
                    connectionString = connection.encrypted,
                    host = connection.target,
                    query = request.query,
                )
            }

            is ValidProbeProtocolRedisRequest -> {
                val connection =
                    resolveConnection(
                        incoming = request.connectionString,
                        existing = (existingProbe?.content as? ProbeContent.Redis)?.connectionString,
                        kind = ConnectionKind.REDIS,
                    )
                ProbeContent.Redis(
                    connectionString = connection.encrypted,
                    host = connection.target,
                    command = request.command,
                )
            }

            is ValidProbeProtocolSmtpRequest ->
                ProbeContent.Smtp(
                    hostname = request.hostname,
                    port = request.port,
                    security = request.security,
                )

            is ValidProbeProtocolKafkaRequest ->
                ProbeContent.Kafka(
                    brokers = request.brokers,
                    topic = request.topic,
                    message = request.message,
                    ssl = request.ssl,
                    allowAutoTopicCreation = request.allowAutoTopicCreation,
                )

            is ValidProbeProtocolRabbitMqRequest ->
                ProbeContent.RabbitMq(
                    managementNodes = request.managementNodes,
                    username = request.username,
                    password =
                        resolveEncryptedValue(
                            incoming = request.password,
                            existing = (existingProbe?.content as? ProbeContent.RabbitMq)?.password,
                            requiredMessage = "RabbitMQ password is required on creation",
                        ).encrypted,
                )

            else -> throw IllegalArgumentException("Unsupported protocol")
        }

    private fun resolveEncryptedValue(
        incoming: String?,
        existing: String?,
        requiredMessage: String,
    ): ResolvedEncryptedValue {
        val supplied = incoming?.takeIf(String::isNotBlank)
        val source = supplied ?: existing ?: throw BadRequestException(requiredMessage)
        val plainText = encryptionService.decryptIfEncrypted(source)
        val encrypted =
            if (supplied == null && encryptionService.isEncrypted(source)) {
                source
            } else {
                encryptionService.encrypt(plainText)
            }

        return ResolvedEncryptedValue(encrypted = encrypted, plainText = plainText)
    }

    private fun resolveConnection(
        incoming: String?,
        existing: String?,
        kind: ConnectionKind,
    ): ResolvedConnection {
        val value =
            resolveEncryptedValue(
                incoming = incoming,
                existing = existing,
                requiredMessage = "${kind.displayName} connection string is required on creation",
            )

        return ResolvedConnection(
            encrypted = value.encrypted,
            target = connectionTarget(value.plainText, kind),
        )
    }

    private fun connectionTarget(
        connectionString: String,
        kind: ConnectionKind,
    ): String {
        val uri = URI(connectionString)
        val host = requireNotNull(uri.host) { "${kind.displayName} host is required" }
        val path = uri.path.takeIf { it.isNotBlank() && it != "/" }.orEmpty()
        require(!kind.pathRequired || path.isNotEmpty()) {
            "${kind.displayName} database is required"
        }
        val port = if (uri.port == -1) kind.defaultPort else uri.port

        return "$host:$port$path"
    }

    private enum class ConnectionKind(
        val displayName: String,
        val defaultPort: Int,
        val pathRequired: Boolean,
    ) {
        POSTGRESQL("PostgreSQL", 5432, true),
        SQL_SERVER("SQL Server", 1433, true),
        MYSQL("MySQL/MariaDB", 3306, true),
        REDIS("Redis", 6379, false),
    }

    private data class ResolvedConnection(
        val encrypted: String,
        val target: String,
    )

    private data class ResolvedEncryptedValue(
        val encrypted: String,
        val plainText: String,
    )
}
