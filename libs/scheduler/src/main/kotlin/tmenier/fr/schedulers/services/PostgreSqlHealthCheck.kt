package tmenier.fr.schedulers.services

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.enterprise.context.ApplicationScoped
import tmenier.fr.common.dtos.ProbeContent
import tmenier.fr.common.encryption.EncryptionService
import java.net.URI
import java.sql.DriverManager
import java.util.Properties

fun interface PostgreSqlHealthCheck {
    fun check(
        content: ProbeContent.PostgreSql,
        timeoutSeconds: Int,
    )
}

@ApplicationScoped
class JdbcPostgreSqlHealthCheck(
    private val encryptionService: EncryptionService,
) : PostgreSqlHealthCheck {
    override fun check(
        content: ProbeContent.PostgreSql,
        timeoutSeconds: Int,
    ) {
        val connection = parseConnectionString(content.connectionString)
        val properties =
            Properties().apply {
                connection.username?.let { setProperty("user", it) }
                connection.password?.let { setProperty("password", it) }
                setProperty("connectTimeout", timeoutSeconds.toString())
                setProperty("socketTimeout", timeoutSeconds.toString())
            }

        DriverManager
            .getConnection(connection.jdbcUrl, properties)
            .use { databaseConnection ->
                databaseConnection.createStatement().use { statement ->
                    statement.queryTimeout = timeoutSeconds
                    statement.execute(content.query)
                }
            }
    }

    internal fun parseConnectionString(connectionString: String): PostgreSqlConnection {
        val uri = URI(encryptionService.decryptIfEncrypted(connectionString))
        require(uri.scheme == "postgres" || uri.scheme == "postgresql") {
            "Connection string must start with postgres:// or postgresql://"
        }
        require(!uri.host.isNullOrBlank()) { "PostgreSQL host is required" }
        require(uri.path.isNotBlank() && uri.path != "/") { "PostgreSQL database is required" }

        val credentials = uri.userInfo?.split(":", limit = 2)

        val port = if (uri.port == -1) 5432 else uri.port
        val host = if (':' in uri.host) "[${uri.host}]" else uri.host
        val parameters = uri.rawQuery?.let { "?$it" }.orEmpty()

        return PostgreSqlConnection(
            jdbcUrl = "jdbc:postgresql://$host:$port${uri.rawPath}$parameters",
            username = credentials?.firstOrNull()?.takeIf { it.isNotBlank() },
            password = credentials?.getOrNull(1),
        )
    }
}

@RegisterForReflection
data class PostgreSqlConnection(
    val jdbcUrl: String,
    val username: String?,
    val password: String?,
)
