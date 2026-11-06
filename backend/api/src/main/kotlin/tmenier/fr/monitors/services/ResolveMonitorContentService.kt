package tmenier.fr.monitors.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import tmenier.fr.databases.dtos.ProbeDTO
import tmenier.fr.monitors.requests.BaseStoreProbeRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolDnsRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolDockerContainerRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolHttpRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolKafkaRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolMongoDbRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolMySqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolOracleRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPingRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPostgreSqlRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolPushRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolRabbitMqRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolRedisRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolSmtpRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolSqlServerRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolTcpRequest
import tmenier.fr.monitors.requests.ValidProbeProtocolWebSocketUpgradeRequest
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

            is ValidProbeProtocolWebSocketUpgradeRequest ->
                ProbeContent.WebSocketUpgrade(
                    url = request.url.trim(),
                    ignoreCertificateErrors = request.ignoreCertificateErrors,
                    subprotocol = request.subprotocol?.trim()?.takeIf { it.isNotEmpty() },
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

            is ValidProbeProtocolOracleRequest -> {
                val connection =
                    resolveConnection(
                        incoming = request.connectionString,
                        existing = (existingProbe?.content as? ProbeContent.Oracle)?.connectionString,
                        kind = ConnectionKind.ORACLE,
                    )
                ProbeContent.Oracle(
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

            is ValidProbeProtocolMongoDbRequest -> {
                val value =
                    resolveEncryptedValue(
                        incoming = request.connectionString,
                        existing = (existingProbe?.content as? ProbeContent.MongoDb)?.connectionString,
                        requiredMessage = "MongoDB connection string is required on creation",
                    )
                ProbeContent.MongoDb(
                    connectionString = value.encrypted,
                    host = mongoDbTarget(value.plainText),
                    command = request.command.trim(),
                )
            }

            is ValidProbeProtocolDockerContainerRequest ->
                ProbeContent.DockerContainer(
                    containerName = request.containerName.trim().removePrefix("/"),
                    dockerHost = request.dockerHost.trim().removeSuffix("/"),
                )

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

            is ValidProbeProtocolPushRequest ->
                ProbeContent.Push(
                    periodSeconds = request.periodSeconds,
                    graceSeconds = request.graceSeconds,
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
            if (supplied == null && encryptionService.isEncryptedWithCurrentKey(source)) {
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

    internal fun mongoDbTarget(connectionString: String): String {
        val srv = connectionString.startsWith("mongodb+srv://")
        require(srv || connectionString.startsWith("mongodb://")) {
            "Connection string must start with mongodb:// or mongodb+srv://"
        }
        val rest = connectionString.substringAfter("://")
        val authority = rest.substringBefore('/').substringBefore('?')
        val hosts = authority.substringAfterLast('@')
        require(hosts.isNotBlank()) { "MongoDB host is required" }

        val withPorts =
            if (srv) {
                hosts
            } else {
                hosts.split(',').joinToString(",") { host ->
                    val hasPort = if (host.startsWith("[")) host.contains("]:") else host.contains(':')
                    if (hasPort) host else "$host:$MONGODB_DEFAULT_PORT"
                }
            }
        val database =
            rest
                .substringAfter(authority, "")
                .removePrefix("/")
                .substringBefore('?')

        return if (database.isBlank()) withPorts else "$withPorts/$database"
    }

    private enum class ConnectionKind(
        val displayName: String,
        val defaultPort: Int,
        val pathRequired: Boolean,
    ) {
        POSTGRESQL("PostgreSQL", 5432, true),
        SQL_SERVER("SQL Server", 1433, true),
        MYSQL("MySQL/MariaDB", 3306, true),
        ORACLE("Oracle", 1521, true),
        REDIS("Redis", 6379, false),
    }

    private companion object {
        const val MONGODB_DEFAULT_PORT = 27017
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
