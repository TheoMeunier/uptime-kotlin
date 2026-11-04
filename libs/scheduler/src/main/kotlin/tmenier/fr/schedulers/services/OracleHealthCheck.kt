package tmenier.fr.schedulers.services

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.sql.DriverManager
import java.util.Properties

fun interface OracleHealthCheck {
    fun check(
        content: ProbeContent.Oracle,
        timeoutSeconds: Int,
    )
}

@ApplicationScoped
class JdbcOracleHealthCheck(
    private val encryptionService: EncryptionService,
) : OracleHealthCheck {
    override fun check(
        content: ProbeContent.Oracle,
        timeoutSeconds: Int,
    ) {
        val connection = parseConnectionString(content.connectionString)
        val timeoutMilliseconds = (timeoutSeconds * 1_000).toString()
        val properties =
            Properties().apply {
                setProperty(PROGRAM_PROPERTY, APPLICATION_NAME)
                connection.properties.forEach { (key, value) -> setProperty(key, value) }
                connection.username?.let { setProperty("user", it) }
                connection.password?.let { setProperty("password", it) }

                setProperty(CONNECT_TIMEOUT_PROPERTY, timeoutMilliseconds)
                setProperty(READ_TIMEOUT_PROPERTY, timeoutMilliseconds)
            }

        DriverManager
            .getConnection(connection.jdbcUrl, properties)
            .use { databaseConnection ->
                databaseConnection.createStatement().use { statement ->
                    statement.queryTimeout = timeoutSeconds
                    statement.execute(normalizeQuery(content.query))
                }
            }
    }

    internal fun parseConnectionString(connectionString: String): OracleConnection {
        val uri = URI(encryptionService.decryptIfEncrypted(connectionString))
        require(uri.scheme == "oracle") { "Connection string must start with oracle://" }
        require(!uri.host.isNullOrBlank()) { "Oracle host is required" }
        require(uri.path.isNotBlank() && uri.path != "/") { "Oracle service name is required" }

        val credentials = uri.userInfo?.split(":", limit = 2)
        val port = if (uri.port == -1) DEFAULT_PORT else uri.port
        val host = if (':' in uri.host && !uri.host.startsWith("[")) "[${uri.host}]" else uri.host
        val serviceName = uri.path.removePrefix("/")

        return OracleConnection(
            jdbcUrl = "jdbc:oracle:thin:@//$host:$port/$serviceName",
            username = credentials?.firstOrNull()?.takeIf { it.isNotBlank() },
            password = credentials?.getOrNull(1),
            properties = parseParameters(uri.rawQuery),
        )
    }

    internal fun normalizeQuery(query: String): String {
        val trimmed = query.trim().ifBlank { ProbeContent.Oracle.DEFAULT_ORACLE_QUERY }
        val isPlSqlBlock = PL_SQL_BLOCK.containsMatchIn(trimmed)
        return if (isPlSqlBlock) trimmed else trimmed.trimEnd(';', ' ', '\n', '\r', '\t')
    }

    private fun parseParameters(rawQuery: String?): Map<String, String> =
        rawQuery
            ?.split("&")
            ?.filter { it.isNotBlank() }
            ?.associate { parameter ->
                val key = parameter.substringBefore('=')
                val value = parameter.substringAfter('=', "")
                decode(key) to decode(value)
            }.orEmpty()

    private fun decode(value: String) = URLDecoder.decode(value, StandardCharsets.UTF_8)

    private companion object {
        const val DEFAULT_PORT = 1521
        const val APPLICATION_NAME = "uptime-kotlin"
        const val PROGRAM_PROPERTY = "v\$session.program"
        const val CONNECT_TIMEOUT_PROPERTY = "oracle.net.CONNECT_TIMEOUT"
        const val READ_TIMEOUT_PROPERTY = "oracle.jdbc.ReadTimeout"
        val PL_SQL_BLOCK = Regex("^(?:BEGIN|DECLARE)\\b", RegexOption.IGNORE_CASE)
    }
}

@RegisterForReflection
data class OracleConnection(
    val jdbcUrl: String,
    val username: String?,
    val password: String?,
    val properties: Map<String, String> = emptyMap(),
)
